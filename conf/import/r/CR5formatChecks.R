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

#Libraries nedeed
library("XML")
library("plyr")
library("stringr")
library("dplyr")
library("RJSONIO")
library("jsonlite")
library("anchors")
library("lubridate")
options(scipen = 999)

Args <- commandArgs(TRUE)

#Find the path to the params file
#==================================================
initial.options <- commandArgs(trailingOnly = FALSE)
#Parameters
JSON.path <- "-paramsFile="
paramsJSON <- fromJSON(sub(JSON.path, "", initial.options[grep(JSON.path, initial.options)]))
cat(paste("JSON.path", sub(JSON.path, "", initial.options[grep(JSON.path, initial.options)]), " \n", sep = ":"))

file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
cat(paste("script.name", script.name, " \n", sep = ":"))


#Set locale to UTF-8
Sys.setlocale("LC_CTYPE", "UTF-8")

# #Load all the scripts
  setwd(dirname(script.name))
  file.sources <- list.files(dirname(script.name),
                             pattern="*.R$",
                             full.names=FALSE,
                             ignore.case=TRUE)
  file.sources <- file.sources[!(file.sources == basename(script.name))]
  cat(paste("file.sources:", file.sources, "\n", sep = ""))
  sapply(file.sources,source,.GlobalEnv)


#Read the database XML
  doc <- xmlParse(paramsJSON$systemDescriptionXMLPath)

#Registry name
  registry.name <- reg.name(doc)

#Parse the variables and dictionary nodes into a dataframe
  doc.data <- xmlToDataFrame(nodes = xmlChildren(xmlRoot(doc)[["variables"]]))
  dic.data <- xmlToDataFrame(nodes = xmlChildren(xmlRoot(doc)[["dictionaries"]]))

#Read dictionaries
  dic.codes <- read.table(paramsJSON$dictionaryFilePath,
                          sep = '\t',
                          header = F,
                          quote = '',
                          comment = '',
                          stringsAsFactors = FALSE,
                          colClasses = "character")

  #It's essencial a dataframe that has the dictionary id and the codes for future checkings
  #The exported dictionary from CanReg5 has the dictionary id and the codes in the same column
  df.codes <- grep("(^[[:punct:]][[:digit:]])", dic.codes$V1, perl = TRUE, value = TRUE)
  df.dic.names <- grep("(^[[:punct:]]{4}[[:alpha:]])", dic.codes$V2, perl = TRUE, value = TRUE)
  dic_idx <- which(dic.codes$V1 %in% df.codes)
  dic_idx <- c(dic_idx, nrow(dic.codes))
  dic.codes.tidy <- data.frame(matrix(ncol = 4, nrow = 0))
  for (i in 1:(length(dic_idx)-1)){
    idx_ini <- dic_idx[i]
    idx_end <- dic_idx[i+1]
    codes.df <- rep(df.codes[i],(idx_end - idx_ini -1))
    dic.names.df <- rep(df.dic.names[i],(idx_end - idx_ini -1))
    aux.df <- cbind(dic.codes[(idx_ini + 1):(idx_end - 1),],codes.df, dic.names.df)
    dic.codes.tidy <- rbind(dic.codes.tidy, aux.df)
  }
  colnames(dic.codes.tidy) <- c("code", "description", "dictionary_id","name_dictionary")

  dic.codes.tidy$dictionary_id <- str_replace(dic.codes.tidy$dictionary_id, "\\#", "")
  dic.codes.tidy$name_dictionary <- str_replace(dic.codes.tidy$name_dictionary, "\\----", "")
  dic.codes.tidy <- merge(dic.codes.tidy,dic.data,by = c("dictionary_id"))
  dic.codes.tidy <- dic.codes.tidy[, c("code", "description", "dictionary_id", "name_dictionary", "full_dictionary_code_length")]

  
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
  
#Import the data file
  #It is necessary to check if the file for Patient, Tumour and Source is the same
  if (paramsJSON$patientFilePath == paramsJSON$tumourFilePath & paramsJSON$tumourFilePath == paramsJSON$sourceFilePath){
    raw.data <- import.fn(paramsJSON$patientFilePath, paramsJSON$patientFileSeparator)
    patient.raw.data <- raw.data
    #It's necessary to match the variables names from the import file with the variables names
    #in the DB
    patient.import.data <- match.names.db(paramsJSON$patientVarNameInImportFile, 
                                          paramsJSON$patientVarNameInDatabase, 
                                          patient.raw.data)
    
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
    
    tumour.raw.data <- raw.data
    #It's necessary to match the variables names from the import file with the variables names
    #in the DB
    tumour.import.data <- match.names.db(paramsJSON$tumourVarNameInImportFile, 
                                         paramsJSON$tumourVarNameInDatabase, 
                                         tumour.raw.data)
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
    
    source.raw.data <- raw.data
    #It's necessary to match the variables names from the import file with the variables names
    #in the DB
    source.import.data <- match.names.db(paramsJSON$sourceVarNameInImportFile, 
                                         paramsJSON$sourceVarNameInDatabase, 
                                         source.raw.data)
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
    format.errors <- data.frame(cbind(patient.raw.data$format.errors,
                                      tumour.raw.data$format.errors,
                                      source.raw.data$format.errors),
                                stringsAsFactors = FALSE)
    #To merge the column names with errors into one column
    #Add this merged column to the raw.data dataframe
    format.errors[format.errors == ""] <- NA
    format.errors$format.errors <-  apply(format.errors, 1, paste, collapse = ", ")
    format.errors$format.errors <- gsub("NA, |, NA|NA","", format.errors$format.errors)
    format.errors$format.errors <- str_replace(format.errors$format.errors, 
                                               ", , ", "")
    raw.data$format.errors <- format.errors$format.errors
    #To replace the raw.data with the checked raw data
    raw.data[,colnames((patient.raw.data)[-ncol(patient.raw.data)])] <- patient.raw.data[, -ncol(patient.raw.data)]
    raw.data[,colnames((tumour.raw.data)[-ncol(tumour.raw.data)])] <- tumour.raw.data[, -ncol(tumour.raw.data)]
    raw.data[,colnames((source.raw.data)[-ncol(patient.raw.data)])] <- source.raw.data[, -ncol(source.raw.data)]
    #To paste the column name to the value it is necessary a auxiliar dataframe
    aux.raw.data <- raw.data[-ncol(raw.data)]
    aux.raw.data[] <- Map(paste, names(aux.raw.data), aux.raw.data, sep = ': ')
    raw.data$all.raw.data <- apply(aux.raw.data, 1, paste, collapse = "\n")
    file.write <- paste(dirname(paramsJSON$patientFilePath),
                        "output.raw.data.csv",
                        sep = "//")
    write.csv(raw.data, file.write,
              row.names = FALSE,
              fileEncoding = "UTF-8")
    try(if(!file.exists(file.write)) stop("The file was not written"))
    
    #Standard output
    cat(paste("-outFile", file.write, sep = ":"))
  }else{
    if (!is.null(paramsJSON$patientFilePath)){
      patient.raw.data <- import.fn(paramsJSON$patientFilePath, paramsJSON$patientFileSeparator)
      #It's necessary to match the variables names from the import file with the variables names
      #in the DB
      patient.import.data <- match.names.db(paramsJSON$patientVarNameInImportFile, 
                                            paramsJSON$patientVarNameInDatabase, 
                                            patient.raw.data)
      
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
      #To write the raw.data file
      patient.raw.data$all.raw.data <-  apply(patient.raw.data[,-ncol(patient.raw.data)], 1, paste, collapse = ", ")
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
      #To write the raw.data file
      tumour.raw.data$all.raw.data <-  apply(tumour.raw.data[,-ncol(tumour.raw.data)], 1, paste, collapse = ", ")
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
      #To write the raw.data file
      source.raw.data$all.raw.data <-  apply(source.raw.data[,-ncol(source.raw.data)], 1, paste, collapse = ", ")
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

#PatientID is the standard variable name for record number, length = 8
  #If the file does not contain the case number, the last record number will be needed
  #patient.id <- as.character(filter(doc.data, standard_variable_name == 'PatientID') %>% select(short_name))

 
  











