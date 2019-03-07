# We must check on the dates:
# 1- delete: / and -
# 2- right format: yyyymmdd
# 3- right length

format.date.fn <- function(doc.data = data.frame,
                           import.data = data.frame,
                           names.raw.data, #variable names in the raw data file
                           dt.type = "character"){#Patient, Tumour or Source
  # First we need to know which columns are dates
  date.columns <- doc.data[doc.data$variable_type == "Date" & doc.data$table == dt.type, c("short_name")]
  if (length(date.columns) > 0){
    #It is going to delete the slash and hyphen
    #Slash: /
    #Hyphen: -
    
    aux.data <- import.data[, date.columns]
    #aux.data.slash$FechaN[2]<-'1946/07/15'
    #aux.data.slash$FUC[2]<-'2012-07-15'
    
    aux.data <- aux.data %>%
      transmute_all(funs(replacesString))
    
    #==========================
    #2- Right length: 8 digits
    #aux.data <- import.data[, date.columns]
    aux.data <- aux.data %>% 
      mutate_all(funs(len = str_length))
    #If the lenght columns are not 0 or 8 the value is wrong
    aux.data[str_detect(names(aux.data),"_len$")] <- lapply(aux.data[str_detect(names(aux.data),"_len$")], 
                                                            function(x) replace(x, x %in% c(0,8), "no error"))
    aux.data[str_detect(names(aux.data),"_len$")] <- lapply(aux.data[str_detect(names(aux.data),"_len$")], 
                                                            function(x) replace(x, (!x %in% c(0,8,"no error")), "error"))
    
    
    aux.data <- pasteDateError(aux.data,
                               date.columns,
                               "(incorrect date format)")
    
    #====================
    #Checking if the format is yyyymmdd
    #There are going to be 2 dataframes: aux.data.yyyymmdd and aux.data.yyyymmdd2
    aux.data.yyyymmdd <- import.data[, date.columns]
    #Coverting the dates to the formats: ymd
    aux.data.yyyymmdd2 <- aux.data.yyyymmdd %>% transmute_all(funs(ToDate))
    aux.data.yyyymmdd2[aux.data.yyyymmdd2 == ""] <- NA
    aux.data.yyyymmdd2[is.na(aux.data.yyyymmdd2)] <- aux.data.yyyymmdd[is.na(aux.data.yyyymmdd2)]
    aux.data.yyyymmdd2[is.na(aux.data.yyyymmdd2)] <- ""
    #Check if all values are dates
    aux.data.yyyymmdd <- aux.data.yyyymmdd2 %>%
      mutate_all(funs(date.yyyymmdd = IsDate))
    aux.data.yyyymmdd <- unknown.date.fn(aux.data,
                                         aux.data.yyyymmdd[str_detect(names(aux.data.yyyymmdd),"_date.yyyymmdd")],
                                         date.columns)
    aux.data <- data.frame(cbind(aux.data.yyyymmdd,
                                 aux.data[str_detect(names(aux.data),"_len")]),
                           stringsAsFactors = FALSE)
    #If the date format es TRUE should be changeg for "no error" and 
    #if it is FALSE should be changed for "error"
    aux.data[str_detect(names(aux.data),"_date.yyyymmdd")] <- lapply(aux.data[str_detect(names(aux.data),"_date.yyyymmdd")], 
                                                                     function(x) replace(x, 
                                                                                         x == TRUE, "no error"))
    aux.data[str_detect(names(aux.data),"_date.yyyymmdd")] <- lapply(aux.data[str_detect(names(aux.data),"_date.yyyymmdd")], 
                                                                     function(x) replace(x, 
                                                                                         (x != "no error"), 
                                                                                         "error"))
    
    #Concatenate the error type to the record value
    
    aux.data <- pasteDateError(aux.data,
                               date.columns,
                               "(incorrect date format)")
    
    #====================
    #It is necessary to merge the length and date format errors into one column
    aux.errors.merged <- mergeDataErrors(aux.data[str_detect(names(aux.data),"_len$")],
                                         aux.data[str_detect(names(aux.data),"_date.yyyymmdd")], 
                                         date.columns)
    aux.data[,date.columns] <- mergeDataErrors(aux.errors.merged,
                                               aux.data.yyyymmdd2, 
                                               date.columns)
    
    aux.errors.merged[aux.errors.merged != "no error"] <- "error"
    #We need to change the column names in the dataset for the column names in the raw data,
    #so the format.error columns is going to help the user to identify which columns have errors
    names.rdt <- names.raw.data[names(import.data) %>% 
                                  match(x = names(aux.errors.merged)[1:length(date.columns)])]
    names(aux.errors.merged) <- names.rdt
    aux.errors.merged[aux.errors.merged == "no error"] <- NA
    #To replace the "errors" for the column name
    aux.errors.merged <- replaceColNameError(aux.errors.merged, 
                                             names.rdt, 
                                             date.columns)
    
    
    #To merge the column names with errors into one column
    aux.data$format.errors <- apply(aux.errors.merged, 1, paste, collapse=", ")
    aux.data$format.errors <- gsub("NA, |, NA|NA","", aux.data$format.errors)
    aux.data$format.errors <- str_replace(aux.data$format.errors, 
                                          ", , ", "")
    colnames(aux.data)[colnames(aux.data) %in% date.columns] <- names.rdt
    return(aux.data[,c(names.rdt,"format.errors")])
  }else{
    return("no dates to check")
  }
  
  
  
  
  
}