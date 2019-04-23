match.names.db <- function(names.import, #column names into the import file
                           names.db, #variables names on CR5 DB
                           dt.data = data.frame){
  #This function matches the variable names in the import file with the variables names into the DB
  #===============================================================================================
  # check if there is any value equal "", if this is TRUE then there are few columns that they
  # need to be generated in order to import the data
  names.import[is.na(names.import)] <- ""
  for (i in 1:length(names.import)){
      if(names.import[i] != ""){
        names(dt.data)[names(dt.data) == names.import[i]] <- names.db[i]
      }else{
        aux.col <- rep("",nrow(dt.data))
        dt.data <- data.frame(cbind(dt.data, aux.col, stringsAsFactors = FALSE),
                              stringsAsFactors = FALSE,
                              check.names = FALSE)
        colnames(dt.data)[which(names(dt.data) == colnames((dt.data)[ncol(dt.data)]))] <- c(names.db[i])
        
      }
    }
  if (length(names.db[!(names.db %in% colnames(dt.data))]) != 0){
    for (i in 1:length(names.db[!(names.db %in% colnames(dt.data))])){
      aux.col <- rep("",nrow(dt.data))
      dt.data <- data.frame(cbind(dt.data, aux.col, stringsAsFactors = FALSE),
                            stringsAsFactors = FALSE,
                            check.names = FALSE)
      colnames(dt.data)[ncol(dt.data)] <- names.db[!(names.db %in% colnames(dt.data))][1]
    }
  }else{NULL}
  return(dt.data[, c(names.db)])

}
