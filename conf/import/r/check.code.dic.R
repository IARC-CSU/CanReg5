check.code.dic <- function(dic.codes.tidy = data.frame,
                           var.dic.data = data.frame,
                           dt = data.frame,
                           type.table = "character",
                           names.raw.data){

  aux.vars <- var.dic.data[var.dic.data$table == type.table,]
  if (nrow(aux.vars) > 0){
    for (i in 1:nrow(aux.vars)){
      if (aux.vars$fill_in_status[i] != "Mandatory"){
        dt$newCol <- dt[, c(aux.vars$short_name[i])] %in% c(dic.codes.tidy$code[dic.codes.tidy$name_dictionary == aux.vars$use_dictionary[i]],"")
        colnames(dt)[ncol(dt)] <- paste(aux.vars$short_name[i], "_error", sep = "")
      }else{
        dt$newCol <- dt[, c(aux.vars$short_name[i])] %in% dic.codes.tidy$code[dic.codes.tidy$name_dictionary == aux.vars$use_dictionary[i]]
        colnames(dt)[ncol(dt)] <- paste(aux.vars$short_name[i], "_error", sep = "")
      }
    }
    head(dt)
    dt[str_detect(names(dt),"_error$")] <- lapply(dt[str_detect(names(dt),"_error$")], 
                                                 function(x) replace(x, x %in% TRUE, "no error"))
    dt[str_detect(names(dt),"_error")] <- lapply(dt[str_detect(names(dt),"_error")], 
                                                 function(x) replace(x, (!x %in% c("no error")), "error"))
    dt <- pasteCodeError(dt,
                         "(there is not this code in the dictionary)")
    
    aux.dt <- dt[str_detect(names(dt),"_error$")]
    aux.dt[aux.dt != "no error"] <- "error"
    names(aux.dt) <- str_replace(names(aux.dt),"_error", "")
    #We need to change the column names in the dataset for the column names in the raw data,
    #so the format.error columns is going to help the user to identify which columns have errors
    names.rdt <- names.raw.data[names(dt) %>% 
                                  match(x = names(aux.dt)[1:length(aux.vars$short_name)])]
    names(aux.dt) <- names.rdt
    aux.dt[aux.dt == "no error"] <- NA
    #To replace the "errors" for the column name
    aux.dt <- replaceColNameError(aux.dt)
    #To merge the column names with errors into one column
    dt$code.errors <- apply(aux.dt, 1, paste, collapse=", ")
    dt$code.errors <- gsub("NA, |, NA|NA","", dt$code.errors)
    dt$code.errors <- str_replace(dt$code.errors, 
                                          ", , ", "")
    dt <- dt[c(aux.vars$short_name, "code.errors")]
    names(dt) <- c(names.rdt, "code.errors")
    return(dt)
  }else{
    return("no dictionaries to check")
  }
}