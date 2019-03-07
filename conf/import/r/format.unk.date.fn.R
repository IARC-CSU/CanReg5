format.unk.date.fn <- function(doc.data,
                               aux.data,
                               dt.type = "character"){
  date.columns <- doc.data[doc.data$variable_type == "Date" & doc.data$table == dt.type, c("short_name")]
  #Detect if the dates column has / or -
  #If they have or dont have / or -, detect if the year is at the end or at the begining
  #Detect if the 2 first digits are month
  aux.data.unknown <- data.frame(lapply(aux.data[,date.columns], 
                                        function(x) str_detect(x,"([0-9][0-9]99[1-2][0|9][0-9][0-9])|(99[0-1][0-9][1-2][0|9][0-9][0-9])|(9999[1-2][0|9][0-9][0-9])|([0-9][0-9][0-9][0-9]9999)")))
  
}