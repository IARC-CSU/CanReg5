 ## To get the R folder of the actual script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", 
                   initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)
  ## to get canreg argument list
Args <- commandArgs(TRUE)

tryCatch({

  ## source function to check if update needed
  source(paste(sep="/", script.basename, "Rcan_core.r"))

  ## check for update and update source

  ## load other function 
  source(paste(sep="/", script.basename, "r-sources", "canreg_core.r"))
  source(paste(sep="/", script.basename, "r-sources", "canreg_table.r"))

  
  canreg_clean_install(Args)

},
error = function(e) {

  if (exists("pb")) {
    close(pb)
  }
  
  sink(type="message")
  sink()
  close(log_connection)
  if (file.exists(filename)) file.remove(filename)
  
   
  #create log error file name 
  log_name <- paste0(gsub("\\W","", label),"_",sc,"_",gsub("\\D","", Sys.time()),"_error_log.txt") 
  
  
  #find path and create log file
  pos <- max(gregexpr("\\", out, fixed=TRUE)[[1]])
  path <- substr(out,start=1, stop=pos)

  log_file <- paste0(path, log_name)
  error_connection <- file(log_file,open="wt")
  sink(error_connection)
  sink(error_connection, type="message")
  
  #print error
  cat(paste0("An error occured! please send the log file: `",log_file,"` to  canreg@iarc.fr\n\n"))
  print(paste("MY_ERROR:  ",e))
  cat("\n")
  #print argument from canreg
  print(Args)
  cat("\n")
  
  #print R version and package load
  print(sessionInfo())
  cat("\n")
  
  #print incidence / population file (r format)
  cat("Incidence file\n")
  dput(read.table(inc, header=TRUE))
  cat("\n")
  cat("population file\n")
  dput(read.table(pop, header=TRUE))
  cat("\n")
  
  #close log_file and send to canreg
  sink(type="message")
  sink()
  close(error_connection)
  cat(paste("-outFile",log_file,sep=":"))
  
  
})
