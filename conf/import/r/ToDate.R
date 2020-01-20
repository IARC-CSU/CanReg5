ToDate <- function(mydate) {
  #This function will convert the dates to yyyyymmdd
  mydate <- parse_date_time(mydate, orders = c("ymd", "dmy", "mdy"))
  mydate<- format.POSIXct(mydate, "%Y%m%d")
  mydate <- replace(mydate,is.na(mydate),"")
}