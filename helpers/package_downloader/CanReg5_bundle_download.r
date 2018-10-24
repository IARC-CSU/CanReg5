
# destination folder for zip
destdir <- "d:/r-package"

#create folder if do not exist
dir.create(file.path(destdir),recursive = TRUE)

# clean destination folder (remove zip file)
unlink(paste0(destdir,"/*.zip"),recursive = TRUE)


# package list
packages_list <- c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","ReporteRs","zip","bmp", "jpeg")

#Select https repos
old.repos <- getOption("repos") 
on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
new.repos <- old.repos 
new.repos["CRAN"] <- "https://cran.r-project.org" #set your favorite  CRAN Mirror here 
options(repos = new.repos) 
  
  
#install library in temp lib 
unlink(file.path(tempdir()),recursive = TRUE)
dir.create(file.path(tempdir()),recursive = TRUE)
.libPaths(file.path(tempdir()))

  
# force binary for windows   
if (Sys.info()[['sysname']] == "Windows") {
  options(pkgType="win.binary") #to avoid package more recent from source
}
  
# intall package and dependencies, keeping zip in destination folder. :)
for (i in packages_list) {
  install.packages(i, destdir = destdir,dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
}
  


