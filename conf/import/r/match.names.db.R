match.names.db <- function(names.import, #column names into the import file
                           names.db, #variables names on CR5 DB
                           import.data = data.frame){
  #This function matches the variable names in the import file with the variables names into the DB
  #===============================================================================================
  # check if there is any value equal "", if this is TRUE then there are few columns that they
  # need to be generated in order to import the data
  for (i in 1:length(names.import)){
      if(names.import[i] != ""){
        names(import.data)[names(import.data) == names.import[i]] <- names.db[i]
      }else{
        aux.col <- rep("",nrow(import.data))
        import.data <- data.frame(cbind(import.data, aux.col, stringsAsFactors = FALSE),
                                 stringsAsFactors = FALSE)
        colnames(import.data)[which(names(import.data) == colnames((import.data)[ncol(import.data)]))] <- c(names.db[i])
        
      }
    }
  
  return(import.data[, c(names.db)])

}
