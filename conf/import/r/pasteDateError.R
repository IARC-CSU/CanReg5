pasteDateError <- function(aux.data, 
                           date.columns, 
                           type.error = "character"){
  #Row and column index where values are wrong
  #Incorrect length or incorrect date format
  errors.data <- data.frame(which(aux.data == "error", arr.ind=T))
  if (nrow(errors.data) > 1){
    col.error <- unique(errors.data$col)
    
    #Replace the error value for the name of the column value
    for (i in 1:length(col.error)){
      aux.data[errors.data$row[errors.data$col == col.error[i]], col.error[i]] <- paste(aux.data[errors.data$row[errors.data$col == col.error[i]], col.error[i] - length(date.columns)], 
                                                                                                                  type.error, 
                                                                                                                  sep = " ")
    }
  }else{
    col.error <- unique(errors.data$col)
    if (nrow(errors.data) == 1){
      aux.data[errors.data$row[errors.data$col == col.error], col.error] <- paste(aux.data[errors.data$row[errors.data$col == col.error], col.error - length(date.columns)], 
                                                                                                            type.error, 
                                                                                                            sep = " ")
      
    }else{NULL}
  }
  return(aux.data)
    
}