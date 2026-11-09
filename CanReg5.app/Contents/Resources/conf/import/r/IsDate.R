IsDate <- function(mydate) {
  aux.dates <- mydate
  tryCatch(!is.na(parse_date_time(mydate, orders = c("ymd", "dmy", "mdy")))| mydate == "",  
           error = function(err) {FALSE})  
}