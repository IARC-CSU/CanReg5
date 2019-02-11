replacesString <- function(mydate, 
                           date.string = "/|-") {
  #index.empty.dates <- which(mydate == "", arr.ind = TRUE)
  #if (length(index.empty.dates) == 0){
  #  mydate <- str_replace_all(mydate, date.string, "")
  #}else{
  #  mydate[index.empty.dates] <- TRUE
  #}
  mydate <- str_replace_all(mydate, date.string, "")
}