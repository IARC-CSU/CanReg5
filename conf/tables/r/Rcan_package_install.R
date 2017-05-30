## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
Args <- commandArgs(TRUE)


packages_list <- c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","ReporteRs")

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
  


cat("This log file contains warnings, errors, and package availability information\n\n")

dir.create(file.path(Sys.getenv("R_LIBS_USER")),recursive = TRUE)
.libPaths(Sys.getenv("R_LIBS_USER"))

missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]

old.repos <- getOption("repos") 
on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
new.repos <- old.repos 

new.repos["CRAN"] <- "https://cloud.r-project.org/" #set your favorite  CRAN Mirror here 

options(repos = new.repos) 





if (!"Rcpp" %in% missing_packages) {
  if (packageVersion("Rcpp") < "0.11.0") {
    missing_packages <- c(missing_packages,"Rcpp" )
  }
}

if (!"ggplot2" %in% missing_packages) {
  if (packageVersion("ggplot2") < "2.2.0") {
    missing_packages <- c(missing_packages,"ggplot2" )
  }
}

if (!"data.table" %in% missing_packages) {
  if (packageVersion("data.table") < "1.9.6") {
    missing_packages <- c(missing_packages,"data.table" )
  }
}

if (!"scales" %in% missing_packages) {
  if (packageVersion("scales") < "0.4.1") {
    missing_packages <- c(missing_packages,"scales" )
  }
}

if ("scales" %in% missing_packages) {
  
  if ("munsell" %in% installed.packages()[,"Package"]) {
    if (packageVersion("munsell") < "0.2") {
      missing_packages <- c(missing_packages,"munsell" )
    }
  }
}

if ("ggplot2" %in% missing_packages) {
  
  if ("gtable" %in% installed.packages()[,"Package"]) {
    if (packageVersion("gtable") < "0.1.1") {
      missing_packages <- c(missing_packages,"gtable" )
    }
  }
  if ("plyr" %in% installed.packages()[,"Package"]) {
    if (packageVersion("plyr") < "1.7.1") {
      missing_packages <- c(missing_packages,"plyr" )
    }
  }
}



if ("ReporteRs" %in% missing_packages) {
  
  if ("rvg" %in% installed.packages()[,"Package"]) {
    if (packageVersion("rvg") < "0.1.2") {
      missing_packages <- c(missing_packages,"rvg")
    }
  }
}


ap <- available.packages()

cat("This is the actual repository\n")

print(getOption("repos"))




for (i in c(packages_list, "rvg","plyr", "gtable","munsell" )) {
  print(paste0(i," available: ", i %in% rownames(ap)))
}

missing_packages <- unique(missing_packages)

if(length(missing_packages) > 0 ) {
  for (i in missing_packages) {
    install.packages(i, dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
  }
}




lapply(packages_list, require, character.only = TRUE)



sink(type="message")
sink()
close(log_connection)
cat(paste("-outFile",filename,sep=":"))


},
error = function(e) {
  
  sink(type="message")
  sink()
  close(log_connection)
  if (file.exists(filename)) file.remove(filename)
  
  #find path and create log file
  pos <- max(gregexpr("\\", out, fixed=TRUE)[[1]])
  path <- substr(out,start=1, stop=pos)
  log_file <- paste0(path, "canreg_log.txt")
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
