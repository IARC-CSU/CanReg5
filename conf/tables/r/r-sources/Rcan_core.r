## version : 1.0

canreg_error_log <- function(e,filename,out,Args,inc,pop) {

  if (exists("pb")) {
    close(pb)
  }
  
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
  cat(paste0("An error occured! please send the log file: `",log_file,"` to canreg@iarc.fr.\n"))
  cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
  print(paste("MY_ERROR:  ",e))
  cat("\n")
  
  #print argument from canreg
  print(Args)
  cat("\n")
  
  #print environment
  print(ls.str())
  cat("\n")
  
  #print R version and package load
  print(sessionInfo())
  cat("\n")

  #print table builder version 
  print(canreg_get_table_builder_version())
  cat("\n")
  
  
  #print java information if windows
  if (Sys.info()[['sysname']] == "Windows") {
    print(canreg_find_java())
  }
  
  #print missing package

  packages_list <- c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png","shiny.i18n", "Rcan")

  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]  
  if (length(missing_packages) == 0) {
    print("No missing package")
  } else {
    print(missing_packages)
  }
  cat("\n")
  
  #test loading package
  lapply(packages_list, require, character.only = TRUE)
  cat("\n")
  
  print("----------------------")
  cat("\n")
  #print incidence / population file (r format)
  cat("Incidence file\n")
  dput(read.table(inc, header=TRUE, sep="\t"))
  cat("\n")
  cat("population file\n")
  dput(read.table(pop, header=TRUE, sep="\t"))
  cat("\n")
  
  #close log_file and send to canreg
  sink(type="message")
  sink()
  close(error_connection)
  
  
  
  
  cat(paste("-outFile",log_file,sep=":"))
  
}



canreg_args <- function(Args) {
  
  
  arg_list <- list()
  
  ## get Args from canreg  
  
  for (logi_name in c("skin","landscape","logr","multi_graph")) {
    
    arg_list[[logi_name]] <- FALSE
  }
  
  for (i in 1:length(Args)) {
    
    temp <- Args[i]
    pos <- regexpr("=",temp)[1]
    
    if (pos < 0) {
      arg_list[[substring(temp,2)]] <- TRUE
    } 
    else {
      varname <- substring(temp,2,pos-1)
      assign(varname,substring(temp, pos+1)) 
      if (suppressWarnings(!is.na(as.numeric(get(varname))))){
        assign(varname, as.numeric(get(varname)))
      }
    }
    arg_list[[varname]] <- get(varname)
  }
  
  ## Get the list of languages from the folder of translations...
  translations_files <- list.files(path=paste0(script.basename, "/r-translations"), pattern= "translation_.*?\\.csv")
  available_translations = sub("translation_(.*)\\.csv", "\\1", translations_files)

  if (!(arg_list[["lang"]] %in% available_translations)) {
    ## see if "mother" variant language is in list
    mlang <- gsub("(..)_.*","\\1",arg_list[["lang"]])
    if (mlang %in% available_translations) {
      arg_list[["lang"]] <- mlang 
    } else {
    ## Set default language to English if the language is not among the translated languages...
    arg_list[["lang"]] <- "en"
    }
  }

  if (substr(arg_list$out,nchar(arg_list$out)-nchar(arg_list$ft),nchar(arg_list$out)) == paste0(".", arg_list$ft)) {
    arg_list[["filename"]] <- arg_list$out
    arg_list$out <- substr(arg_list$out,1,nchar(arg_list$out)-nchar(arg_list$ft)-1)
  } else {
    arg_list[["filename"]] <- paste(arg_list$out, arg_list$ft, sep = "." )
  }
  
  
  return(list=arg_list)
  
  
}



canreg_load_packages <- function(packages_list) { 


  sysName <- Sys.info()[['sysname']]

  timeStart<-Sys.time()


  if (sysName == "Windows") {
    pb <- winProgressBar(
    title = "Download R packages",
    label = "Initializing"
    )
  } 


  if (getRversion() == '3.2.0') {
     
    stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
    
  }

  #add curl to loading source from github
  # packages_list <- c("curl", packages_list)
    
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
          label = sprintf("%s - (This might take a while...)", missing_packages[i])
        )
      }

      install.packages(missing_packages[i], dependencies=  FALSE, quiet = TRUE)


    }

  }

  # ensure all package dependencies are installed
  if (sysName == "Windows") {
    setWinProgressBar(pb, 0, label = "Ensuring package dependencies ...")
  }
  all_pck <- getDependencies(packages_list, installed=FALSE, available=TRUE)
  missing_packages <- all_pck[!(all_pck %in% list_installed_packages)]

  if(length(missing_packages) > 0 ) {

    for (i in seq_along(missing_packages)) {

      if (sysName == "Windows") {
        setWinProgressBar(
          pb, 
          value = i / (length(missing_packages) + 1),
          label = sprintf("%s - (I hope you are sitting comfortably...)", missing_packages[i])
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
    close(pb)
  }

  timeEnd<-Sys.time()
  difference <- difftime(timeEnd, timeStart, units='mins')
  print(difference)

}

canreg_check_update <- function()
{

   #check internet
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 


  new.repos["CRAN"] <- "https://cran.r-project.org" #set your favorite  CRAN Mirror here 

  options(repos = new.repos)  
  ap <- available.packages()
  if (length(ap) == 0) {
    stop("CanReg5 can't access the internet and download the R packages. Please try again later.") 
  }

  # need to add test for internet
  remote_source_folder <- "https://raw.githubusercontent.com/IARC-CSU/CanReg5/master/conf/tables/r/r-sources/"
  remote_shiny_folder <- "https://raw.githubusercontent.com/IARC-CSU/CanReg5/master/conf/tables/r/shiny/"

  if (canreg_update_source(paste0(remote_source_folder,"versions.txt")))
  {
    if (canreg_update_source(paste0(remote_source_folder,"Rcan_core.r")))
    {
      source(paste(sep="/", script.basename, "r-sources", "Rcan_core.r"))
    }

    canreg_update_source(paste0(remote_source_folder,"canreg_table.r"))
    canreg_update_source(paste0(remote_source_folder,"canreg_core.r"))
    canreg_update_source(paste0(remote_source_folder,"shiny_core.r"))
    canreg_update_source(paste0(remote_source_folder,"News.txt"))

    canreg_update_source(paste0(remote_source_folder,"CI5_alldata.rds"), TRUE)
    canreg_update_source(paste0(remote_source_folder,"CI5_data.rds"), TRUE)

    canreg_update_source(paste0(remote_shiny_folder,"global.r"))
    canreg_update_source(paste0(remote_shiny_folder,"server.r"))
    canreg_update_source(paste0(remote_shiny_folder,"ui.r"))

  }

}


canreg_update_source <- function (url, data=FALSE) {

  bool <- FALSE

  filename <- regmatches(url, regexpr("[^\\/]*\\.[a-zA-Z]+?$", url))
  folder_base <- regmatches(script.basename, regexpr("^.+?conf", script.basename))
  folder_url <- regmatches(url, regexpr("conf.+?$", url))
  folder_url <- regmatches(folder_url, regexpr("\\/.+?$", folder_url))
  local_file <- paste0(folder_base, folder_url)

  if (data)
  {

    dt_temp <- readRDS(local_file)
    local_version <- attr(dt_temp, "version")

    dt_temp <- readRDS(url(url))
    remote_version <- attr(dt_temp, "version")

  }
  else {
    con <- file(local_file,"r")
    file_text <- readLines(con,n=1)
    local_version <- regmatches(file_text, regexpr("\\d\\.\\d+", file_text))
    close(con)

    con <- file(url,"r")
    file_text <- readLines(con,n=1)
    remote_version <- regmatches(file_text, regexpr("\\d\\.\\d+", file_text))
    close(con)

  }

  if (remote_version > local_version) 
  {
      download.file(url, local_file)
      bool = TRUE
  }

  return(bool)
  

}

canreg_get_table_builder_version <- function()
{
  local_file <- paste0(script.basename, "/r-sources/versions.txt")
  con <- file(local_file,"r")
  file_text <- readLines(con,n=1)
  local_version <- regmatches(file_text, regexpr("\\d\\.\\d+", file_text))
  close(con)

  return(paste0("TB version: ", local_version))

}




