mergeDataErrors <- function(aux.data.length,
                            aux.data.ymd,
                            column.names){
  #This function will merge the length and date format errors into one column
  aux.data.length[aux.data.length == "no error"] <- NA
  aux.data.length[is.na(aux.data.length)] <- aux.data.ymd[is.na(aux.data.length)]
  names(aux.data.length) <- column.names
  return(aux.data.length)
}