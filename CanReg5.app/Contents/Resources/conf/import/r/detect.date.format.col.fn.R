detect.date.format.col.fn <- function(aux.df.date){
  if (aux.df.date %in% c(28,29,30,31,32)){
    aux.df.date <- "dd"
  }else{
    if (aux.df.date %in% c(12,13,14)){
      aux.df.date <- "mm"
    }else{
      aux.df.date <- "yyyy"
    }
  }
  return(aux.df.date)
}