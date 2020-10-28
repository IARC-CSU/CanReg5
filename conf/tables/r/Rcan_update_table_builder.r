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
  source(paste(sep="/", script.basename, "r-sources", "Rcan_core.r"))
  canreg_check_update()

  cat(paste("-outFile",paste(sep="/", script.basename, "r-sources", "News.txt"), sep=":"))



},
error = function(e) {

  print(paste("MY_ERROR:  ",e))
  cat("\n")
  #print argument from canreg
  print(Args)
  cat("\n")
  
  #print R version and package load
  print(sessionInfo())
  cat("\n")

})
