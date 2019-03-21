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
  
  aux.data.split <- lapply(aux.data[,date.columns],
                           function(x) colsplit(x,"/|-", c("date1","date2","date3")))
  
  #unlist just one element from the aux.data.split to detect the date format
  aux.df <- do.call("cbind", aux.data.split[1])
  aux.df.date1 <- detect.date.format.col.fn(nrow(table(aux.df[1])))
  aux.df.date2 <- detect.date.format.col.fn(nrow(table(aux.df[2])))
  aux.df.date3 <- detect.date.format.col.fn(nrow(table(aux.df[3])))

  
  for (i in 1:length(date.columns)){
    if (i == 1){
      colsplit.names <- c(paste(date.columns[i], rep(1:3), sep = "."))
    }else{
      colsplit.names <- c(colsplit.names,
                          paste(date.columns[i], rep(1:3), sep = "."))
    }
  }
  return(paste(aux.df.date1, aux.df.date2, aux.df.date3, sep = ""))
}