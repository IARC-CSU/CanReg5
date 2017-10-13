load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))               #this resets the repos option when the function exits 
  new.repos <- old.repos 
  new.repos["CRAN"] <- "http://cran.rstudio.com/"   #Rstudio, automatic redirection to servers worldwide , or set your favorite  CRAN Mirror here 
  options(repos = new.repos)
  x <- as.character(substitute(x))
  rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)
  dir.create(rlibs, recursive = TRUE)
  eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', quiet = TRUE )", sep=""))) 
  eval(parse(text=paste("require(", x, ", lib.loc = '", rlibs ,"')", sep="")))
}

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

#if(!is.installed("ggplot2")){
#	load.fun("ggplot2")
#}

#require(ggplot2)

canreg_load_packages <- function(packages_list) { 
  
  
  if (getRversion() == '3.2.0') {
   
    stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
    
  }
  
  dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
  .libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))
  
  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]
  
  #managing installing package for old R version. 
  if (getRversion() < '3.2.0') {
    utils::setInternet2(TRUE)
    if (Sys.info()[['sysname']] == "Windows") {
      options(download.file.method = "internal")
    } else if (Sys.info()[['sysname']] == "Linux") {
      options(download.file.method = "wget")
    } else if (Sys.info()[['sysname']] == "Darwin") {
      options(download.file.method = "curl")
    }
  } else if (getRversion() < '3.3.0') {
    if (Sys.info()[['sysname']] == "Windows") {
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


  missing_packages <- unique(missing_packages)
  


  if(length(missing_packages) > 0 ) {
    
    if (Sys.info()[['sysname']] == "Windows") {
      options(pkgType="win.binary") #to avoid package from source which need compilation.
    }
    
    for (i in missing_packages) {
      install.packages(i, dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)

    }
  }
  
  lapply(packages_list, require, character.only = TRUE)
  
}


canreg_load_packages(c("ggplot2", "Cairo", "RColorBrewer"))
