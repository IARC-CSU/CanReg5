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


  sysName <- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <- winProgressBar(
    title = "Download R packages",
    label = "Initializing"
    )
  } 


  if (getRversion() == '3.2.0') {
     
    stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
    
  }
    
  dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
  .libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))

  list_installed_packages <- installed.packages()[,"Package"]
  missing_packages <- packages_list[!(packages_list %in% list_installed_packages)]

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

  new.repos["CRAN"] <- "https://cran.r-project.org"
  options(repos = new.repos) 

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

  if (!"officer" %in% missing_packages) {
    if (packageVersion("officer") < "0.2.2") {
      missing_packages <- c(missing_packages,"officer" )
    }
  }

  if (!"flextable" %in% missing_packages) {
    if (packageVersion("flextable") < "0.5.2") {
      missing_packages <- c(missing_packages,"flextable" )
    }
  }

  #to avoid package from source which need compilation.
  if (sysName == "Windows") {
    options(pkgType="win.binary") 
  }

  missing_packages <- unique(missing_packages)

  if (!"gtools" %in% list_installed_packages) {
    install.packages("gtools", dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
  }

  require("gtools", character.only = TRUE)


  if(length(missing_packages) > 0 ) {

    all_pck <- getDependencies(missing_packages, installed=FALSE, available=TRUE)
    missing_packages <- c(missing_packages, all_pck)

    for (i in seq_along(missing_packages)) {

      if (sysName == "Windows") {
        setWinProgressBar(
          pb, 
          value = i / (length(missing_packages) + 1),
          label = sprintf("downloading package - %s", missing_packages[i])
        )
      }

      install.packages(missing_packages[i], dependencies=  FALSE, quiet = TRUE)


    }

  }

  # ensure all package dependencies are installed
  setWinProgressBar(pb, 0, label = "Ensuring package dependencies ...")
  all_pck <- getDependencies(packages_list, installed=FALSE, available=TRUE)
  missing_packages <- all_pck[!(all_pck %in% list_installed_packages)]

  if(length(missing_packages) > 0 ) {

    for (i in seq_along(missing_packages)) {

      if (sysName == "Windows") {
        setWinProgressBar(
          pb, 
          value = i / (length(missing_packages) + 1),
          label = sprintf("downloading package - %s", missing_packages[i])
        )
      }

      install.packages(missing_packages[i], dependencies=  FALSE, quiet = TRUE)
    }
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
  }

  close(pb)

}

canreg_load_packages(c("ggplot2", "Cairo", "RColorBrewer"))
