## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

canreg_get_user_lib <- function() {
  r_maj_min <- paste0(getRversion()$major, ".", getRversion()$minor)
  r_user_env <- Sys.getenv("R_LIBS_USER")
  if (nzchar(r_user_env)) {
    expanded <- gsub("%v", r_maj_min, r_user_env, fixed = TRUE)
    if (!grepl(paste0("R-?", getRversion()$major), expanded, ignore.case = TRUE) &&
        !grepl(r_maj_min, expanded, fixed = TRUE)) {
      user_lib <- paste0(expanded, "-R", r_maj_min, "-CanReg5")
    } else {
      user_lib <- paste0(expanded, "-CanReg5")
    }
  } else {
    user_lib <- file.path(path.expand("~"), paste0(".R-", r_maj_min, "-CanReg5-library"))
  }
  user_lib <- normalizePath(user_lib, winslash = "/", mustWork = FALSE)
  return(user_lib)
}

user_lib <- canreg_get_user_lib()
if (!dir.exists(user_lib)) {
  try(dir.create(user_lib, recursive = TRUE, showWarnings = FALSE), silent = TRUE)
}

filename <- file.path(user_lib, "log_install.txt")
print(filename)
log_connection <- file(filename, open = "wt")

sink(log_connection)
sink(log_connection, type="message")

#check if bundle package exist
setwd(script.basename)
packages  <- list.files(pattern=".*\\.zip$")

if (length(packages) < 10)
{
	cat("This log file contains warnings, errors, and package availability information\n\n")
	cat("This canreg version does not include the R packages bundle\n")
	cat("Either:\n") 
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

	unlink(user_lib, recursive = TRUE)
	dir.create(user_lib, recursive = TRUE)
	.libPaths(unique(c(user_lib, .libPaths())))
	rlibs <- gsub("\\", "/", user_lib, fixed = TRUE)
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