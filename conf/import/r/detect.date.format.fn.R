detect.date.format.fn <- function(aux.data,
                                   doc.data,
                                   dt.type = "character"){
  date.columns <- doc.data[doc.data$variable_type == "Date" & doc.data$table == dt.type, c("short_name")]
  aux.data.unknown <- data.frame(lapply(aux.data[,date.columns], 
                                        function(x) str_detect(x,"/|-")))
  aux.data.unknown.loc <- data.frame(lapply(aux.data[,date.columns], 
                                            function(x) str_locate(x,"/|-")))
  aux.data.8 <- data.frame(lapply(aux.data[,date.columns], 
                                  function(x) str_remove_all(x,"/|-")))
  for (i in 1:length(date.columns)){
    if (i == 1){
      colsplit.names <- c(paste(date.columns[i], rep(1:3), sep = "."))
    }else{
      colsplit.names <- c(colsplit.names,
                          paste(date.columns[i], rep(1:3), sep = "."))
    }
  }
  if (any(aux.data.unknown.loc == 3)){
    #Date can be dd/mm/yyyy or mm/dd/yyyy
    aux.data.split <- data.frame(lapply(aux.data[,date.columns], 
                                        function(x) str_split(x,"/|-")))
  }else{
    #Date can be yyyy/mm/dd or yyyy/dd/mm
  }
  for(i in 1:ncol(aux.data.unknown))
  return(aux.data.unknown)
}