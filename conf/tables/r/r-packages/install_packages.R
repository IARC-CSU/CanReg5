## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)


filename <- paste0(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"), "/log_install.txt")
print(filename)
log_connection <- file(filename,open="wt")

sink(log_connection)
sink(log_connection, type="message")

#check if bundle package exist
setwd(script.basename)
packages  <- list.files(pattern=".*\\.zip$")

if (length(packages) < 10)
{
	cat("This log file contains warnings, errors, and package availability information\n\n")
	cat("This canreg version does not include the R packages bundle")
	cat("Either:") 
	cat("* Use R clean install in the table builder to download R packages from internet\n\n")
	cat("* Install CanReg5 with the R bundle\n\n")
} else 
{

  options(warn = 1)
  sysName <- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <- winProgressBar(
    	title = "install all R packages",
    	label = "Initializing"
    )

    setWinProgressBar(pb, 0.01,label = "I'm not responding cause I'm busy.. please wait")

  } 

   # Clean install in r\3.4\canreg5.
	unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
	dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
	.libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))
	rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)
	i <- 1
	for(x in packages) {


		if (sysName == "Windows") {
      setWinProgressBar(
        pb, 
        value = i / (length(packages) + 1),
        label = sprintf("%s (This might take a while...)",x)
      )
    }

	  eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep="")))

	  i <- i + 1
	}
}





sink(type="message")
sink()
close(log_connection)
cat(paste("-outFile",filename,sep=":"))






# ## Detect all package in bundle folder
# packages  <- list.files(pattern=".*\\.zip$")

# for(x in packages) {
#     eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep=""))) 
# }