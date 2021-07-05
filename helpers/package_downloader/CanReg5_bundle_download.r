## Bundle for CanReg5 realease 44g. 2020/10/21 + I18n packages for translations


# destination folder for zip
destdir <- "c:/tmp"

#create folder if do not exist
dir.create(file.path(destdir),recursive = TRUE)

# clean destination folder (remove zip file)
unlink(paste0(destdir,"/*.zip"),recursive = TRUE)

# package list
packages_list <- c("Rcpp", "data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png", "shiny.i18n","Rcan", "XML", "plyr", "stringr", "dplyr", "RJSONIO", "jsonlite", "anchors", "lubridate", "reshape2", "tidyr", "commonmark", "gtools","cpp11")

#Select https repos
old.repos <- getOption("repos") 
on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
new.repos <- old.repos 
new.repos["CRAN"] <- "https://cran.r-project.org" #set your favorite  CRAN Mirror here 
options(repos = new.repos) 


#install library in temp lib 
unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-temp")),recursive = TRUE)
dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-temp")),recursive = TRUE)
.libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-temp"))

# force binary for windows   
if (Sys.info()[['sysname']] == "Windows") {
  options(pkgType="win.binary") #to avoid package more recent from source
}

# intall package and dependencies, keeping zip in destination folder. :)
for (i in packages_list) {
  install.packages(i, destdir = destdir,dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
}

unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-temp")),recursive = TRUE)


