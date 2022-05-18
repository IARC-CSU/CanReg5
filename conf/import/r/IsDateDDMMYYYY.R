IsDateDDMMYYYY <- function(mydate, 
                   date.format = "%d%m%Y") {
  tryCatch(!is.na(as.Date(mydate,date.format)),  
           error = function(err) {FALSE})  
  
}