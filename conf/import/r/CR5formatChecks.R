#Import patient, tumour and source files
#After importing it is mandatory to check:
#-If all the dataset have the same quantity of records
#-If there are ID: PatientRecordID, TumourRecordID and SourceRecordID. The program should obtain the ID into the DB and should check
#if there are records with the same IDs
#-If PatientID is in the file but there are not PatientRecordID, TumourRecordID and SourceRecordID,
#the script is going to create the IDs based on these IDs. Note: it should check if the IDs are consecutives

#All the variables names in the file and in the DB are going to be passed
# one array with the DB names (short_names)
# one array with the variable names in the file
# each DB array has to be ordered on the same way of the file array
# separated arrays for each table: patient, tumour and source will be necessary

#Libraries
#======================================================
.libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))
#======================================================

#Libraries nedeed
library("XML")
library("plyr")
library("stringr")
library("dplyr")
library("RJSONIO")
library("jsonlite")
library("anchors")
library("lubridate")
library("reshape2")
library("tidyr")
options(scipen = 999)

Args <- commandArgs(TRUE)

#Find the path to the params file
#==================================================
initial.options <- commandArgs(trailingOnly = FALSE)
#Parameters
JSON.path <- "-paramsFile="
paramsJSON <- fromJSON(sub(JSON.path, "", initial.options[grep(JSON.path, initial.options)]))
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])

#Set locale to UTF-8
if(.Platform$OS.type != "windows"){
  Sys.setlocale("LC_CTYPE", "UTF-8")
}else{NULL}


#Load all the scripts
  setwd(dirname(script.name))
  file.sources <- list.files(dirname(script.name),
                             pattern="*.R$",
                             full.names=FALSE,
                             ignore.case=TRUE)
  file.sources <- file.sources[!(file.sources == basename(script.name))]
  sapply(file.sources,source,.GlobalEnv)


#Read the database XML
  doc <- xmlParse(paramsJSON$systemDescriptionXMLPath)

#Registry name
  registry.name <- reg.name(doc)

#Parse the variables and dictionary nodes into a dataframe
  doc.data <- xmlToDataFrame(nodes = xmlChildren(xmlRoot(doc)[["variables"]]))
  dic.data <- xmlToDataFrame(nodes = xmlChildren(xmlRoot(doc)[["dictionaries"]]), stringsAsFactors = FALSE)

#Read dictionaries
  dic.codes <- read.table(paramsJSON$dictionaryFilePath,
                          sep = '\t',
                          header = F,
                          quote = '',
                          comment = '',
                          stringsAsFactors = FALSE,
                          colClasses = "character",
                          fileEncoding = "UTF-8")

  #It's essencial a dataframe that has the dictionary id and the codes for future checkings
  #The exported dictionary from CanReg5 has the dictionary id and the codes in the same column
  dic.codes.tidy <- dict.codes.fn(dic.codes)
  
#Variables with Dictionaries
  patient.dic <- doc.data[doc.data$variable_type == "Dict" & doc.data$table == "Patient", c("full_name", "short_name")]
  source.dic <- doc.data[doc.data$variable_type == "Dict" & doc.data$table == "Source", c("full_name", "short_name")]
  tumour.dic <- doc.data[doc.data$variable_type == "Dict" & doc.data$table == "Tumour", c("full_name", "short_name")]

#Order the doc.data dataframe by table and variable_id
  doc.data <- doc.data[with(doc.data,order(table,variable_id)),]

#Complete variable_length in the doc.data so we could put leading zeros in the variables 
  #that use dictionary data
  doc.data <- var.length(doc.data, dic.data)

#It is essential to ad an extra column to the dic.codes.tidy dataset to later filter the codes subgroup
  dic.codes.tidy$lengthMatched_fullLength <- str_length(dic.codes.tidy$code)
#It is necessary to ommit the dictionary groups
  dic.codes.tidy <- dic.codes.tidy %>% filter(lengthMatched_fullLength == full_dictionary_code_length)
  
#Which variables use dictionaries
  var.dic.data <- doc.data[doc.data$variable_type == "Dict", c("short_name", "use_dictionary", "table", "fill_in_status")]
  
#To Uppercase names in the database
  paramsJSON$patientVarNameInDatabase <- toupper(paramsJSON$patientVarNameInDatabase)
  paramsJSON$tumourVarNameInDatabase <- toupper(paramsJSON$tumourVarNameInDatabase)
  paramsJSON$sourceVarNameInDatabase <- toupper(paramsJSON$sourceVarNameInDatabase)
  
#Change colnames in paramsJSON that are null/NA
  paramsJSON$patientVarNameInImportFile[is.na(paramsJSON$patientVarNameInImportFile)] <- paramsJSON$patientVarNameInDatabase[is.na(paramsJSON$patientVarNameInImportFile)]
  paramsJSON$tumourVarNameInImportFile[is.na(paramsJSON$tumourVarNameInImportFile)] <- paramsJSON$tumourVarNameInDatabase[is.na(paramsJSON$tumourVarNameInImportFile)]
  paramsJSON$sourceVarNameInImportFile[is.na(paramsJSON$sourceVarNameInImportFile)] <- paramsJSON$sourceVarNameInDatabase[is.na(paramsJSON$sourceVarNameInImportFile)]
#Import the data file 
  #It is necessary to check if the file for Patient, Tumour and Source is the same
  if (!is.null(paramsJSON$patientFilePath) & !is.null(paramsJSON$tumourFilePath) & !is.null(paramsJSON$sourceFilePath)){
    if (paramsJSON$patientFilePath == paramsJSON$tumourFilePath & paramsJSON$tumourFilePath == paramsJSON$sourceFilePath){
      raw.data <- import.fn(paramsJSON$patientFilePath, paramsJSON$patientFileSeparator)
      patient.raw.data <- raw.data
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      patient.import.data <- match.names.db(paramsJSON$patientVarNameInImportFile, 
                                            paramsJSON$patientVarNameInDatabase, 
                                            patient.raw.data)
      
      #Add leading zeros
      patient.import.data <- leading.zeros(patient.import.data, doc.data, "Patient")
      
      #To generate ids
      PatientID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "PatientID"])
      if (all(patient.import.data[,PatientID.short.name] == "")){
        patient.import.data <- generate.id("patient", patient.import.data, PatientID.short.name)
        patient.raw.data[,PatientID.short.name] <- patient.import.data[,PatientID.short.name]
        patient.raw.data$PATIENTRECORDID <- patient.import.data$PATIENTRECORDID
      }else{NULL}
      
      
      
      #To check dictionary codes
      patient.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  patient.import.data, 
                                                  "Patient", 
                                                  paramsJSON$patientVarNameInImportFile,
                                                  paramsJSON$patientVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(patient.dict.checked.data) == "data.frame"){
        patient.raw.data[,colnames(patient.dict.checked.data)] <- patient.dict.checked.data
        colnames(patient.raw.data)[which(names(patient.raw.data) == "code.errors")] <- "patient.code.errors"
      }else{
        patient.raw.data$patient.code.errors <- ""
      }
      
      #To check the date formats
      patient.checked.raw.data <- format.date.fn(doc.data, 
                                                 patient.import.data, 
                                                 paramsJSON$patientVarNameInImportFile,
                                                 "Patient")
      
      
      
      #To replace the checked columns into the raw data
      if(class(patient.checked.raw.data) == "data.frame"){
        patient.raw.data[,colnames(patient.checked.raw.data)] <- patient.checked.raw.data
        colnames(patient.raw.data)[which(names(patient.raw.data) == "format.errors")] <- "patient.format.errors"
      }else{
        patient.raw.data$patient.format.errors <- ""
      }
      
      
      #===Tumour data
      tumour.raw.data <- patient.raw.data
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      tumour.import.data <- match.names.db(paramsJSON$tumourVarNameInImportFile, 
                                           paramsJSON$tumourVarNameInDatabase, 
                                           tumour.raw.data)
      #Add leading zeros
      tumour.import.data <- leading.zeros(tumour.import.data, doc.data, "Tumour")
      
      #To generate ids
      TumourID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "TumourID"])
      if (all(tumour.import.data[,TumourID.short.name] == "")){
        tumour.import.data <- generate.id("tumour", tumour.import.data, TumourID.short.name)
        tumour.raw.data[,TumourID.short.name] <- tumour.import.data[,TumourID.short.name]
        tumour.raw.data$PATIENTIDTUMOURTABLE <- tumour.import.data$PATIENTIDTUMOURTABLE
        tumour.raw.data$PATIENTRECORDIDTUMOURTABLE <- tumour.import.data$PATIENTRECORDIDTUMOURTABLE
        
      }else{NULL}
      
      
      #To check dictionary codes
      tumour.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                 var.dic.data, 
                                                 tumour.import.data, 
                                                 "Tumour", 
                                                 paramsJSON$tumourVarNameInImportFile,
                                                 paramsJSON$tumourVarNameInImportFile)
      
      #To replace the dictionary checked columns into the raw data
      if(class(tumour.dict.checked.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.dict.checked.data)] <- tumour.dict.checked.data
        colnames(tumour.raw.data)[which(names(tumour.raw.data) == "code.errors")] <- "tumour.code.errors"
      }else{
        tumour.raw.data$tumour.code.errors <- ""
      }
      
      #To check the date formats
      tumour.checked.raw.data <- format.date.fn(doc.data, 
                                                tumour.import.data, 
                                                paramsJSON$tumourVarNameInImportFile,
                                                "Tumour")
      #To replace the checked columns into the raw data
      if(class(tumour.checked.raw.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.checked.raw.data)] <- tumour.checked.raw.data
        colnames(tumour.raw.data)[which(names(tumour.raw.data) == "format.errors")] <- "tumour.format.errors"
      }else{
        tumour.raw.data$tumour.format.errors <- ""
      }
      #===Source data
      source.raw.data <- tumour.raw.data
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      source.import.data <- match.names.db(paramsJSON$sourceVarNameInImportFile, 
                                           paramsJSON$sourceVarNameInDatabase, 
                                           source.raw.data)
      #Add leading zeros
      if(!all(paramsJSON$sourceVarNameInDatabase %in% c("TUMOURIDSOURCETABLE", "SOURCERECORDID"))){
        source.import.data <- leading.zeros(source.import.data, doc.data, "Source")
      }else{NULL}
      
      
      #To generate ids
      SourceID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "SourceRecordID"])
      if (all(source.import.data[,SourceID.short.name] == "")){
        source.import.data <- generate.id("source", source.import.data, SourceID.short.name)
        source.raw.data[,SourceID.short.name] <- source.import.data[,SourceID.short.name]
        source.raw.data$TumourIDSourceTable <- source.import.data$TumourIDSourceTable
      }else{NULL}
      
      #To check dictionary codes
      source.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  source.import.data, 
                                                  "Source", 
                                                  paramsJSON$sourceVarNameInImportFile,
                                                 paramsJSON$sourceVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(source.dict.checked.data) == "data.frame"){
        source.raw.data[,colnames(source.dict.checked.data)] <- source.dict.checked.data
        colnames(source.raw.data)[which(names(source.raw.data) == "code.errors")] <- "source.code.errors"
      }else{
        source.raw.data$source.code.errors <- ""
      }
      
      #To check the date formats
      source.checked.raw.data <- format.date.fn(doc.data, 
                                                source.import.data, 
                                                paramsJSON$sourceVarNameInImportFile,
                                                "Source")
      #To replace the checked columns into the raw data
      if(class(source.checked.raw.data) == "data.frame"){
        source.raw.data[,colnames(source.checked.raw.data)] <- source.checked.raw.data
        colnames(source.raw.data)[which(names(source.raw.data) == "format.errors")] <- "source.format.errors"
      }else{
        source.raw.data$sourceformat.errors <- ""
      }
      
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(patient.raw.data$patient.format.errors,
                                        tumour.raw.data$tumour.format.errors,
                                        source.raw.data$source.format.errors,
                                        patient.raw.data$patient.code.errors,
                                        tumour.raw.data$tumour.code.errors,
                                        source.raw.data$source.code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      raw.data <- source.raw.data[-(ncol(source.raw.data)-1)]
      raw.data$format.errors <- format.errors$format.errors
      #To replace the raw.data with the checked raw data
      raw.data[,names(raw.data) %in% names(patient.raw.data[, !(names(patient.raw.data) %in% c("patient.code.errors", "patient.format.errors"))])] <- patient.raw.data[, !(names(patient.raw.data) %in% c("patient.code.errors", "patient.format.errors"))]
      raw.data[,names(raw.data) %in% names(tumour.raw.data[, !(names(tumour.raw.data) %in% c("tumour.code.errors", "tumour.format.errors"))])] <- tumour.raw.data[, !(names(tumour.raw.data) %in% c("tumour.code.errors", "tumour.format.errors"))]
      raw.data[,names(raw.data) %in% names(source.raw.data[, !(names(source.raw.data) %in% c("source.code.errors", "source.format.errors"))])] <- source.raw.data[, !(names(source.raw.data) %in% c("source.code.errors", "source.format.errors"))]
      
      #To paste the column name to the value it is necessary a auxiliar dataframe
      aux.raw.data <- raw.data[-ncol(raw.data)]
      aux.raw.data[] <- Map(paste,"<strong>",names(aux.raw.data), ": </strong>", aux.raw.data, sep = '')
      raw.data$all.raw.data <- apply(aux.raw.data, 1, paste, collapse = "@#$")
      file.write <- paste(dirname(paramsJSON$patientFilePath),
                          "output.raw.data.csv",
                          sep = "//")
      write.csv(raw.data, file.write,
                row.names = FALSE,
                fileEncoding = "UTF-8")
      try(if(!file.exists(file.write)) stop("The file was not written"))
      
      #Standard output
      cat(paste("-outFile:", file.write, "\n", sep = ""))
    }else{
      #======Patient data
      patient.raw.data <- import.fn(paramsJSON$patientFilePath, paramsJSON$patientFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      patient.import.data <- match.names.db(paramsJSON$patientVarNameInImportFile, 
                                            paramsJSON$patientVarNameInDatabase, 
                                            patient.raw.data)
      
      #Add leading zeros
      patient.import.data <- leading.zeros(patient.import.data, doc.data, "Patient")
      
      #To generate ids
      PatientID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "PatientID"])
      if (all(patient.import.data[,PatientID.short.name] == "")){
        patient.import.data <- generate.id("patient", patient.import.data, PatientID.short.name)
        patient.raw.data[,PatientID.short.name] <- patient.import.data[,PatientID.short.name]
        patient.raw.data$PATIENTRECORDID <- patient.import.data$PATIENTRECORDID
      }else{NULL}
      
      #To check dictionary codes
      patient.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  patient.import.data, 
                                                  "Patient", 
                                                  paramsJSON$patientVarNameInImportFile,
                                                  paramsJSON$patientVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(patient.dict.checked.data) == "data.frame"){
        patient.raw.data[,colnames(patient.dict.checked.data)] <- patient.dict.checked.data
      }else{
        patient.raw.data$code.errors <- ""
      }
      
      #To check the date formats
      patient.checked.raw.data <- format.date.fn(doc.data, 
                                                 patient.import.data, 
                                                 paramsJSON$patientVarNameInImportFile,
                                                 "Patient")
      
      #To replace the checked columns into the raw data
      if(class(patient.checked.raw.data) == "data.frame"){
        patient.raw.data[,colnames(patient.checked.raw.data)] <- patient.checked.raw.data
      }else{
        patient.raw.data$format.errors <- ""
      }
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(patient.raw.data$format.errors,
                                        patient.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      patient.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.patient.raw.data <- patient.raw.data[-ncol(patient.raw.data)]
      aux.patient.raw.data[] <- Map(paste,"<strong>",names(aux.patient.raw.data), ": </strong>", aux.patient.raw.data, sep = '')
      patient.raw.data$all.raw.data <- apply(aux.patient.raw.data, 1, paste, collapse = "@#$")
      file.patient.write <- paste(dirname(paramsJSON$patientFilePath),
                                  "output.patient.raw.data.csv",
                                  sep = "//")
      write.csv(patient.raw.data, file.patient.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.patient.write)) stop("The patient file was not written"))
      #Standard output
      cat(paste("-outPatientFile:", file.patient.write,  "\n", sep = ""))
      #===================
      
      #======Tumour data
      tumour.raw.data <- import.fn(paramsJSON$tumourFilePath, paramsJSON$tumourFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      tumour.import.data <- match.names.db(paramsJSON$tumourVarNameInImportFile, 
                                           paramsJSON$tumourVarNameInDatabase, 
                                           tumour.raw.data)
      
      #Add leading zeros
      tumour.import.data <- leading.zeros(tumour.import.data, doc.data, "Tumour")
      
      #To generate ids
      TumourID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "TumourID"])
      if (all(tumour.import.data[,TumourID.short.name] == "")){
        tumour.import.data <- generate.id("tumour", tumour.import.data, TumourID.short.name)
        tumour.raw.data[,TumourID.short.name] <- tumour.import.data[,TumourID.short.name]
        tumour.raw.data$PATIENTIDTUMOURTABLE <- tumour.import.data$PATIENTIDTUMOURTABLE
        tumour.raw.data$PATIENTRECORDIDTUMOURTABLE <- tumour.import.data$PATIENTRECORDIDTUMOURTABLE
        
      }else{NULL}
      
      
      #To check dictionary codes
      tumour.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  tumour.import.data, 
                                                  "Tumour", 
                                                  paramsJSON$tumourVarNameInImportFile,
                                                 paramsJSON$tumourVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(tumour.dict.checked.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.dict.checked.data)] <- tumour.dict.checked.data
      }else{
        tumour.raw.data$code.errors <- ""
      }
      
      #To check the date formats
      tumour.checked.raw.data <- format.date.fn(doc.data, 
                                                tumour.import.data, 
                                                paramsJSON$tumourVarNameInImportFile,
                                                "Tumour")
      #To replace the checked columns into the raw data
      if(class(tumour.checked.raw.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.checked.raw.data)] <- tumour.checked.raw.data
      }else{
        tumour.raw.data$format.errors <- ""
      }
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(tumour.raw.data$format.errors,
                                        tumour.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      tumour.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.tumour.raw.data <- tumour.raw.data[-ncol(tumour.raw.data)]
      aux.tumour.raw.data[] <- Map(paste,"<strong>",names(aux.tumour.raw.data), ": </strong>", aux.tumour.raw.data, sep = '')
      tumour.raw.data$all.raw.data <- apply(aux.tumour.raw.data, 1, paste, collapse = "@#$")
      
      file.tumour.write <- paste(dirname(paramsJSON$tumourFilePath),
                                 "output.tumour.raw.data.csv",
                                 sep = "//")
      write.csv(tumour.raw.data, file.tumour.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.tumour.write)) stop("The tumour file was not written"))
      #Standard output
      cat(paste("-outTumourFile:", file.tumour.write,  "\n", sep = ""))
      #==================
      
      #=====Source data
      source.raw.data <- import.fn(paramsJSON$sourceFilePath, paramsJSON$sourceFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      source.import.data <- match.names.db(paramsJSON$sourceVarNameInImportFile, 
                                           paramsJSON$sourceVarNameInDatabase, 
                                           source.raw.data)
      
      #Add leading zeros
      if(!all(paramsJSON$sourceVarNameInDatabase %in% c("TUMOURIDSOURCETABLE", "SOURCERECORDID"))){
        source.import.data <- leading.zeros(source.import.data, doc.data, "Source")
      }else{NULL}
      
      
      #To generate ids
      SourceID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "SourceRecordID"])
      if (all(source.import.data[,SourceID.short.name] == "")){
        source.import.data <- generate.id("source", source.import.data, SourceID.short.name)
        source.raw.data[,SourceID.short.name] <- source.import.data[,SourceID.short.name]
        source.raw.data$TumourIDSourceTable <- source.import.data$TumourIDSourceTable
      }else{NULL}
      
      #To check dictionary codes
      source.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  source.import.data, 
                                                  "Source", 
                                                  paramsJSON$sourceVarNameInImportFile,
                                                 paramsJSON$sourceVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(source.dict.checked.data) == "data.frame"){
        source.raw.data[,colnames(source.dict.checked.data)] <- source.dict.checked.data
      }else{
        source.raw.data$code.errors <- ""
      }
      
      #To check the date formats
      source.checked.raw.data <- format.date.fn(doc.data, 
                                                source.import.data, 
                                                paramsJSON$sourceVarNameInImportFile,
                                                "Source")
      #To replace the checked columns into the raw data
      if(class(source.checked.raw.data) == "data.frame"){
        source.raw.data[,colnames(source.checked.raw.data)] <- source.checked.raw.data
      }else{
        source.raw.data$format.errors <- ""
      }
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(source.raw.data$format.errors,
                                        source.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      source.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.source.raw.data <- source.raw.data[-ncol(source.raw.data)]
      aux.source.raw.data[] <- Map(paste,"<strong>",names(aux.source.raw.data), ": </strong>", aux.source.raw.data, sep = '')
      source.raw.data$all.raw.data <- apply(aux.source.raw.data, 1, paste, collapse = "@#$")
      
      file.source.write <- paste(dirname(paramsJSON$sourceFilePath),
                                 "output.source.raw.data.csv",
                                 sep = "//")
      write.csv(source.raw.data, file.source.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.source.write)) stop("The source file was not written"))
      #Standard output
      cat(paste("-outSourceFile:", file.source.write, "\n", sep = ""))
      #==================
    }
  }else{
    if (!is.null(paramsJSON$patientFilePath)){
      patient.raw.data <- import.fn(paramsJSON$patientFilePath, paramsJSON$patientFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      patient.import.data <- match.names.db(paramsJSON$patientVarNameInImportFile, 
                                            paramsJSON$patientVarNameInDatabase, 
                                            patient.raw.data)
      
      #Add leading zeros
      patient.import.data <- leading.zeros(patient.import.data, doc.data, "Patient")
      
      #To generate ids
      PatientID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "PatientID"])
      if (all(patient.import.data[,PatientID.short.name] == "")){
        patient.import.data <- generate.id("patient", patient.import.data, PatientID.short.name)
        patient.raw.data[,PatientID.short.name] <- patient.import.data[,PatientID.short.name]
        patient.raw.data$PATIENTRECORDID <- patient.import.data$PATIENTRECORDID
      }else{NULL}
      
      #To check dictionary codes
      patient.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  patient.import.data, 
                                                  "Patient", 
                                                  paramsJSON$patientVarNameInImportFile,
                                                  paramsJSON$patientVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(patient.dict.checked.data) == "data.frame"){
        patient.raw.data[,colnames(patient.dict.checked.data)] <- patient.dict.checked.data
      }else{
        patient.raw.data$code.errors <- ""
      }
      #To check the date formats
      patient.checked.raw.data <- format.date.fn(doc.data, 
                                                 patient.import.data, 
                                                 paramsJSON$patientVarNameInImportFile,
                                                 "Patient")
      #To replace the checked columns into the raw data
      if(class(patient.checked.raw.data) == "data.frame"){
        patient.raw.data[,colnames(patient.checked.raw.data)] <- patient.checked.raw.data
      }else{
        patient.raw.data$format.errors <- ""
      }
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(patient.raw.data$format.errors,
                                        patient.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      patient.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.patient.raw.data <- patient.raw.data[-ncol(patient.raw.data)]
      aux.patient.raw.data[] <- Map(paste,"<strong>",names(aux.patient.raw.data), ": </strong>", aux.patient.raw.data, sep = '')
      patient.raw.data$all.raw.data <- apply(aux.patient.raw.data, 1, paste, collapse = "@#$")
      
      
      file.patient.write <- paste(dirname(paramsJSON$patientFilePath),
                                  "output.patient.raw.data.csv",
                                  sep = "//")
      write.csv(patient.raw.data, file.patient.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.patient.write)) stop("The patient file was not written"))
      #Standard output
      cat(paste("-outPatientFile:", file.patient.write,  "\n", sep = ""))
      
    }else{
      patient.raw.data <- "No patient data to check"
      patient.import.data <- "No patient data to check"
      patient.checked.raw.data <- "No patient data to check"
    }
    if (!is.null(paramsJSON$tumourFilePath)){
      tumour.raw.data <- import.fn(paramsJSON$tumourFilePath, paramsJSON$tumourFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      tumour.import.data <- match.names.db(paramsJSON$tumourVarNameInImportFile, 
                                           paramsJSON$tumourVarNameInDatabase, 
                                           tumour.raw.data)
      #Add leading zeros
      tumour.import.data <- leading.zeros(tumour.import.data, doc.data, "Tumour")
      
      #To generate ids
      TumourID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "TumourID"])
      if (all(tumour.import.data[,TumourID.short.name] == "")){
        tumour.import.data <- generate.id("tumour", tumour.import.data, TumourID.short.name)
        tumour.raw.data[,TumourID.short.name] <- tumour.import.data[,TumourID.short.name]
        tumour.raw.data$PATIENTIDTUMOURTABLE <- tumour.import.data$PATIENTIDTUMOURTABLE
        tumour.raw.data$PATIENTRECORDIDTUMOURTABLE <- tumour.import.data$PATIENTRECORDIDTUMOURTABLE
        
      }else{NULL}
      
      
      #To check dictionary codes
      tumour.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  tumour.import.data, 
                                                  "Tumour", 
                                                  paramsJSON$tumourVarNameInImportFile,
                                                 paramsJSON$tumourVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(tumour.dict.checked.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.dict.checked.data)] <- tumour.dict.checked.data
      }else{
        tumour.raw.data$code.errors <- ""
      }
      
      #To check the date formats
      tumour.checked.raw.data <- format.date.fn(doc.data, 
                                                tumour.import.data, 
                                                paramsJSON$tumourVarNameInImportFile,
                                                "Tumour")
      #To replace the checked columns into the raw data
      if(class(tumour.checked.raw.data) == "data.frame"){
        tumour.raw.data[,colnames(tumour.checked.raw.data)] <- tumour.checked.raw.data
      }else{
        tumour.raw.data$format.errors <- ""
      }
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(tumour.raw.data$format.errors,
                                        tumour.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      tumour.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.tumour.raw.data <- tumour.raw.data[-ncol(tumour.raw.data)]
      aux.tumour.raw.data[] <- Map(paste,"<strong>",names(aux.tumour.raw.data), ": </strong>", aux.tumour.raw.data, sep = '')
      tumour.raw.data$all.raw.data <- apply(aux.tumour.raw.data, 1, paste, collapse = "@#$")
      
      file.tumour.write <- paste(dirname(paramsJSON$tumourFilePath),
                                 "output.tumour.raw.data.csv",
                                 sep = "//")
      write.csv(tumour.raw.data, file.tumour.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.tumour.write)) stop("The tumour file was not written"))
      #Standard output
      cat(paste("-outTumourFile:", file.tumour.write,  "\n", sep = ""))
      
    }else{
      tumour.raw.data <- "No tumour data to check"
      tumour.import.data <- "No tumour data to check"
      tumour.checked.raw.data <- "No tumour data to check"
    }
    if (!is.null(paramsJSON$sourceFilePath)){
      source.raw.data <- import.fn(paramsJSON$sourceFilePath, paramsJSON$sourceFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      source.import.data <- match.names.db(paramsJSON$sourceVarNameInImportFile, 
                                           paramsJSON$sourceVarNameInDatabase, 
                                           source.raw.data)
      #Add leading zeros
      if(!all(paramsJSON$sourceVarNameInDatabase %in% c("TUMOURIDSOURCETABLE", "SOURCERECORDID"))){
        source.import.data <- leading.zeros(source.import.data, doc.data, "Source")
      }else{NULL}
      
      
      #To generate ids
      SourceID.short.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "SourceRecordID"])
      if (all(source.import.data[,SourceID.short.name] == "")){
        source.import.data <- generate.id("source", source.import.data, SourceID.short.name)
        source.raw.data[,SourceID.short.name] <- source.import.data[,SourceID.short.name]
        source.raw.data$TumourIDSourceTable <- source.import.data$TumourIDSourceTable
      }else{NULL}
      
      #To check dictionary codes
      source.dict.checked.data <- check.code.dic(dic.codes.tidy, 
                                                  var.dic.data, 
                                                  source.import.data, 
                                                  "Source", 
                                                  paramsJSON$sourceVarNameInImportFile,
                                                 paramsJSON$sourceVarNameInDatabase)
      
      #To replace the dictionary checked columns into the raw data
      if(class(source.dict.checked.data) == "data.frame"){
        source.raw.data[,colnames(source.dict.checked.data)] <- source.dict.checked.data
      }else{
        source.raw.data$code.errors <- ""
      }
      
      #To check the date formats
      source.checked.raw.data <- format.date.fn(doc.data, 
                                                source.import.data, 
                                                paramsJSON$sourceVarNameInImportFile,
                                                "Source")
      #To replace the checked columns into the raw data
      if(class(source.checked.raw.data) == "data.frame"){
        source.raw.data[,colnames(source.checked.raw.data)] <- source.checked.raw.data
      }else{
        source.raw.data$format.errors <- ""
      }
      
      #===================
      #To merge the checks
      format.errors <- data.frame(cbind(source.raw.data$format.errors,
                                        source.raw.data$code.errors),
                                  stringsAsFactors = FALSE)
      #To merge the column names with errors into one column
      #Add this merged column to the raw.data dataframe
      format.errors[format.errors == ""] <- NA
      format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
      format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
      format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                                 ", , ", "")
      source.raw.data$format.errors <- format.errors$format.errors
      
      #To write the raw.data file
      aux.source.raw.data <- source.raw.data[-ncol(source.raw.data)]
      aux.source.raw.data[] <- Map(paste,"<strong>",names(aux.source.raw.data), ": </strong>", aux.source.raw.data, sep = '')
      source.raw.data$all.raw.data <- apply(aux.source.raw.data, 1, paste, collapse = "@#$")
      
      file.source.write <- paste(dirname(paramsJSON$sourceFilePath),
                                 "output.source.raw.data.csv",
                                 sep = "//")
      write.csv(source.raw.data, file.source.write, 
                row.names = FALSE,
                fileEncoding = "UTF-8")
      #Ask if the file exists
      try(if(!file.exists(file.source.write)) stop("The source file was not written"))
      #Standard output
      cat(paste("-outSourceFile:", file.source.write, "\n", sep = ""))
    }else{
      source.raw.data <- "No source data to check"
      source.import.data <- "No source data to check"
      source.checked.raw.data <- "No source data to check"
    }
  }
  











