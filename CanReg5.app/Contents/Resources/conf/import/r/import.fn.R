import.fn <- function (filepath = 'character',
                       separator = 'character'){
  #This function imports the file with the data
  if (separator == 'Comma' || separator == ','){
    import.data <- read.csv(filepath, 
                            header = TRUE, 
                            stringsAsFactors = FALSE, 
                            colClasses = 'character', 
                            check.names = FALSE)
  }else if (separator == 'Tab' || separator == '\t'){
    import.data <- read.delim(filepath, 
                              header = TRUE, 
                              sep = "\t", 
                              dec = ".", 
                              colClasses = 'character', 
                              check.names = FALSE)
  }else if (separator == 'Semicolon' || separator == ';'){
    import.data <- read.delim(filepath, 
                              header = TRUE, 
                              sep = ";", 
                              dec = ".", 
                              colClasses = 'character', 
                              check.names = FALSE)
  }else if (separator == 'Pipe' || separator == '|'){
    import.data <- read.delim(filepath, 
                              header = TRUE, 
                              sep = "|", 
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
