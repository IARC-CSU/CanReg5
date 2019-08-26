import.fn <- function (filepath = 'character',
                       separator = 'character'){
  #This function imports the file with the data
  if (separator == 'Comma'){
    import.data <- read.csv(filepath, 
                            header = TRUE, 
                            stringsAsFactors = FALSE, 
                            colClasses = 'character', 
                            check.names = FALSE)
  }else if (separator == 'Tab'){
    import.data <- read.delim(filepath, 
                              header = TRUE, 
                              sep = "\t", 
                              dec = ".", 
                              colClasses = 'character', 
                              check.names = FALSE)
  }else{
    import.data <- read.delim(filepath, 
                              header = TRUE, 
                              sep = separator, 
                              dec = ".", 
                              colClasses = 'character', 
                              check.names = FALSE)
  }

  return(import.data)
}
