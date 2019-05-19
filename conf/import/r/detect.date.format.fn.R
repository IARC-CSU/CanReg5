detect.date.format.fn <- function(aux.data,
                                   doc.data,
                                   dt.type = "character"){
  date.columns <- doc.data[doc.data$variable_type == "Date" & doc.data$table == dt.type, c("short_name")]
  aux.data.unknown <- data.frame(lapply(aux.data[,date.columns], 
                                        function(x) str_detect(x,"/|-")))
  aux.data.8 <- data.frame(lapply(aux.data[,date.columns], 
                                  function(x) str_remove_all(x,"/|-")),
                           stringsAsFactors = FALSE)
  if (ncol(aux.data.8) == length(date.columns)){
    aux.data.4.6 <- aux.data.8
    aux.data.2.4 <- aux.data.8
    for(col in date.columns) {
      aux.data.4.6 <- separate(aux.data.4.6, col, into = paste("date", col, seq(1:3)), sep = c(4,6))
      aux.data.2.4 <- separate(aux.data.2.4, col, into = paste("date", col, seq(1:3)), sep = c(2,4))
    }
    date.format.4.6.array <- rep(1:ncol(aux.data.4.6))
    date.format.2.4.array <- rep(1:ncol(aux.data.2.4))
    for(i in 1: ncol(aux.data.4.6)) {
      date.format.4.6.array[i] <- detect.date.format.col.fn(nrow(table(aux.data.4.6[i])))
      date.format.2.4.array[i] <- detect.date.format.col.fn(nrow(table(aux.data.2.4[i])))
    }
    if (nrow(table(date.format.4.6.array)) == 3){
      aux.df.date1 <- date.format.4.6.array[1]
      aux.df.date2 <- date.format.4.6.array[2]
      aux.df.date3 <- date.format.4.6.array[3]
    }else{
      if (nrow(table(date.format.2.4.array)) == 3){
        aux.df.date1 <- date.format.2.4.array[1]
        aux.df.date2 <- date.format.2.4.array[2]
        aux.df.date3 <- date.format.2.4.array[3]
      }else{NULL}
    }
    if(exists("aux.df.date1")){
      mes <- paste(aux.df.date1, aux.df.date2, aux.df.date3, sep = "")
    }else{
      mes <- "R couldn't get the format date"
    }
  }else{
    mes <- "R couldn't get the format date"
  }
  
  return(mes)
}