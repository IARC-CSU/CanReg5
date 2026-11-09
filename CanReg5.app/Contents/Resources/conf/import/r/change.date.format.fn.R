change.date.format.fn <- function(aux.data,
                                  errors.index,
                                  date.format,
                                  date.columns){
  #CanReg5 date format is "yyyymmdd"
  if (class(errors.index) != "character"){
    if(date.format == "ddmmyyyy"){
      for (i in 1:nrow(errors.index)){
        aux.date <- aux.data[errors.index$row[i], date.columns[errors.index$col[i]]]
        aux.data[errors.index$row[i], date.columns[errors.index$col[i]]] <- paste(substr(aux.date,7,10),
                                                                                  substr(aux.date,4,5),
                                                                                  substr(aux.date,1,2),
                                                                                  sep = "")
      }
    }else{
      if(date.format == "mmddyyyy"){
        for (i in 1:nrow(errors.index)){
          aux.date <- aux.data[errors.index$row[i], date.columns[errors.index$col[i]]]
          aux.data[errors.index$row[i], date.columns[errors.index$col[i]]] <- paste(substr(aux.date,7,10),
                                                                                    substr(aux.date,1,2),
                                                                                    substr(aux.date,4,5),
                                                                                    sep = "")
        }
      }else{
        if(date.format == "yyyymmdd"){
          for (i in 1:nrow(errors.index)){
            aux.date <- aux.data[errors.index$row[i], date.columns[errors.index$col[i]]]
            aux.data[errors.index$row[i], date.columns[errors.index$col[i]]] <- paste(substr(aux.date,1,4),
                                                                                      substr(aux.date,6,7),
                                                                                      substr(aux.date,9,10),
                                                                                      sep = "")
          }
        }else{
          for (i in 1:nrow(errors.index)){
            aux.date <- aux.data[errors.index$row[i], date.columns[errors.index$col[i]]]
            aux.data[errors.index$row[i], date.columns[errors.index$col[i]]] <- paste(substr(aux.date,1,4),
                                                                                      substr(aux.date,9,10),
                                                                                      substr(aux.date,6,7),
                                                                                      sep = "")
          }
        }
      }
    }
  }else{NULL}
  
  return(aux.data)
}