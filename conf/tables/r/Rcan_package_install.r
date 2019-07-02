## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
Args <- commandArgs(TRUE)

## 2019-07-02(ME): This list now contains packages from both analysis and format checking... (starting with XML)
packages_list <- c("Rcpp", "data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png", "shiny.i18n","Rcan", "XML", "plyr", "stringr", "dplyr", "RJSONIO", "jsonlite", "anchors", "lubridate", "reshape2", "tidyr")

## get Args from canreg  
skin <- FALSE
landscape <- FALSE
logr <- FALSE
multi_graph <- FALSE

for (i in 1:length(Args)) {
  
  temp <- Args[i]
  pos <- regexpr("=",temp)[1]
  
  if (pos < 0) {
    assign(substring(temp,2), TRUE)
  } 
  else {
    varname <- substring(temp,2,pos-1)
    assign(varname,substring(temp, pos+1)) 
    if (suppressWarnings(!is.na(as.numeric(get(varname))))){
      assign(varname, as.numeric(get(varname)))
    }
  }
}

ft <- "txt"

#create filename from out and avoid double extension (.pdf.pdf)
if (substr(out,nchar(out)-nchar(ft),nchar(out)) == paste0(".", ft)) {
  filename <- out
  out <- substr(out,1,nchar(out)-nchar(ft)-1)
} else {
  filename <- paste(out, ft, sep = "." )
}


log_connection <- file(filename,open="wt")

sink(log_connection)
sink(log_connection, type="message")

tryCatch({
  
options(warn = 1)
sysName <- Sys.info()[['sysname']]

if (sysName == "Windows") {
  pb <- winProgressBar(
  title = "Download all R packages",
  label = "Initializing"
  )
} 

  


cat("This log file contains warnings, errors, and package availability information\n\n")

if (getRversion() == '3.2.0') {
  
  stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
  
}



#managing installing package for old R version.
if (getRversion() < '3.2.0') {
  utils::setInternet2(TRUE)
  if (sysName == "Windows") {
    options(download.file.method = "internal")
  } else if (sysName == "Linux") {
    options(download.file.method = "wget")
  } else if (sysName == "Darwin") {
    options(download.file.method = "curl")
  }
} else if (getRversion() < '3.3.0') {
  if (sysName == "Windows") {
    options(download.file.method = "wininet")
  } else {
    options(download.file.method = "libcurl")
  }
}
  
  

old.repos <- getOption("repos") 
on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
new.repos <- old.repos 


new.repos["CRAN"] <- "https://cran.r-project.org" #set your favorite  CRAN Mirror here 

options(repos = new.repos) 


ap <- available.packages()

for (i in c(packages_list, "gtools")) {

  bool_internet <- FALSE
  if (i %in% rownames(ap)) {
	  bool_internet <- TRUE
  }
}

if (!bool_internet) {
  stop("Canreg can not access internet and download the R packages. Please try again later") 
}


if (sysName == "Windows") {
  setWinProgressBar(pb, 0,label = "Cleaning canreg R folder")
}
unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
.libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))


cat("This is the actual repository\n")
print(getOption("repos"))

for (i in c(packages_list, "gtools")) {
  print(paste0(i," available: ", i %in% rownames(ap)))
  
}

#to avoid package from source which need compilation.
if (sysName == "Windows") {
  options(pkgType="win.binary") 
}

if (sysName == "Windows") {
  setWinProgressBar(pb, 0,label = "Downloading gtools")
}

install.packages("gtools", dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
require("gtools", character.only = TRUE)

all_pck <- getDependencies(packages_list, installed=FALSE, available=TRUE)
missing_packages <- c(packages_list, all_pck)

for (i in seq_along(missing_packages)) {

  if (sysName == "Windows") {
    setWinProgressBar(
      pb, 
      value = i / (length(missing_packages) + 1),
      label = sprintf("%s - Make yourself at home, this can be long", missing_packages[i])
    )
  }

  install.packages(missing_packages[i], dependencies=  FALSE, quiet = TRUE)

}

if (sysName == "Windows") {
  setWinProgressBar(pb, 0, title = "Loading R packages",label = "Initializing")
}

packages_list <- c(packages_list,"grid")

for (i in seq_along(packages_list)) {

  if (sysName == "Windows") {
    setWinProgressBar(
      pb, 
      value = i / (length(packages_list) + 1),
      label = sprintf("Loading package - %s", packages_list[i])
    )
  }

  require(packages_list[i], character.only = TRUE)

}


if (sysName == "Windows") {
  setWinProgressBar(pb, 1, title = "Loading R packages",label = "Done")
  close(pb)
}





sink(type="message")
sink()
close(log_connection)
cat(paste("-outFile",filename,sep=":"))


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
