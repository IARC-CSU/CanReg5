replaceColNameError <- function(aux.data,
                                names.rdt,
                                date.columns){
  #This function will replace the "errors" for the column name
  errors.data <- data.frame(which(aux.data == "error", arr.ind=T))
  if (nrow(errors.data) > 1){
    col.error <- unique(errors.data$col)
    #Replace the error value for the name of the column value
    for (i in 1:length(col.error)){
      aux.data <- replace.value(aux.data, 
                                names(aux.data)[col.error][i],
                                "error",
                                names(aux.data)[col.error][i])
      
    }
  }else{
    col.error <- unique(errors.data$col)
    if (nrow(errors.data) == 1){
      aux.data <- replace.value(aux.data, 
                                names(aux.data)[col.error],
                                "error",
                                names.rdt[col.error])
      
    }else{NULL}
  }
  return(aux.data)
  
}