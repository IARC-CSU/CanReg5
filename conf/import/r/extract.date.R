extract.date <- function(aux.data){
  #df <- separate(aux.data, 1, sep = c(4,6), into = c("date_", "date_","date_"))
  df <- separate_(aux.data, colnames(aux.data), sep = c(4,6), into = c("date_", "date_","date_"))
}
