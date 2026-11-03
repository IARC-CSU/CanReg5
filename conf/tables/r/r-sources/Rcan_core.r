## version : 1.13

canreg_error_log <- function(e, filename = NULL, out = NULL, Args = NULL, inc = NULL, pop = NULL) {

  if (exists("pb") && !is.null(pb)) {
    try(close(pb), silent = TRUE)
  }
  
  ## get Args from canreg  
  skin <- FALSE
  landscape <- FALSE
  logr <- FALSE
  multi_graph <- FALSE
  sc <- "canreg"
  label <- "error"
  
  if (!missing(Args) && !is.null(Args) && length(Args) > 0) {
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
  }

  cat(paste0("Error in CanReg5 R analysis: ", conditionMessage(e), "\n"), file = stderr())
  
  #create log error file name 
  log_name <- paste0(gsub("\\W","", label),"_",sc,"_",gsub("\\D","", Sys.time()),"_error_log.txt") 

  #find path and create log file
  path <- tempdir()
  if (!missing(out) && !is.null(out) && nzchar(out)) {
    if (file.exists(dirname(out))) {
      path <- dirname(out)
    } else {
      pos <- max(gregexpr("[/\\\\]", out)[[1]])
      if (pos > 0) {
        p <- substr(out, start = 1, stop = pos)
        if (dir.exists(p)) path <- p
      }
    }
  }
  log_file <- file.path(path, log_name)
  error_connection <- tryCatch(file(log_file, open="wt"), error = function(err) NULL)
  if (!is.null(error_connection)) {
    sink(error_connection)
    sink(error_connection, type="message")
    
    #print error
    cat(paste0("An error occured! please send the log file: `",log_file,"` to canreg@iarc.fr.\n"))
    cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
    print(paste("MY_ERROR:  ", conditionMessage(e)))
    cat("\n")
    
    #print argument from canreg
    if (!missing(Args) && !is.null(Args)) {
      print(Args)
      cat("\n")
    }
    
    #print environment
    print(ls.str())
    cat("\n")
    
    #print R version and package load
    print(sessionInfo())
    cat("\n")

    #print table builder version 
    if (exists("canreg_get_table_builder_version", mode = "function")) {
      print(canreg_get_table_builder_version())
      cat("\n")
    }
    
    #print java information if windows
    if (Sys.info()[['sysname']] == "Windows" && exists("canreg_find_java", mode = "function")) {
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
    if (!missing(inc) && !is.null(inc) && file.exists(inc)) {
      cat("Incidence file\n")
      try(dput(read.table(inc, header=TRUE, sep="\t")), silent = TRUE)
      cat("\n")
    }
    if (!missing(pop) && !is.null(pop) && file.exists(pop)) {
      cat("population file\n")
      try(dput(read.table(pop, header=TRUE, sep="\t")), silent = TRUE)
      cat("\n")
    }
    
    #close log_file and send to canreg
    sink(type="message")
    sink()
    close(error_connection)
    
    cat(paste("-outFile",log_file,sep=":"))
  }
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
      val <- substring(temp, pos+1)
      if (suppressWarnings(!is.na(as.numeric(val)))){
        val <- as.numeric(val)
      }
      arg_list[[varname]] <- val
    }
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

  if (exists("winProgressBar", mode = "function")) {
    pb <- winProgressBar(
      title = "Loading R packages",
      label = "Initializing"
    )
  } else {
    pb <- NULL
  }

  if (getRversion() == '3.2.0') {
    stop("The table builder does not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' or later is recommended.\n You can edit the Path in the 'Options' in CanReg.") 
  }

  r_user_env <- Sys.getenv("R_LIBS_USER")
  if (nzchar(r_user_env)) {
    user_lib <- paste0(r_user_env, "-CanReg5")
  } else {
    user_lib <- file.path(path.expand("~"), ".R-CanReg5-library")
  }
  if (!dir.exists(user_lib)) {
    try(dir.create(user_lib, recursive = TRUE, showWarnings = FALSE), silent = TRUE)
  }

  active_libs <- c()
  if (dir.exists(user_lib)) {
    active_libs <- c(active_libs, user_lib)
  }

  # Add bundled library locations if present
  sb <- NULL
  if (exists("script.basename", envir = .GlobalEnv)) {
    sb <- get("script.basename", envir = .GlobalEnv)
  } else if (exists("script.basename", inherits = TRUE)) {
    sb <- get("script.basename")
  }
  if (!is.null(sb) && nzchar(sb)) {
    bundled_libs <- c(
      file.path(sb, "r-packages", "library"),
      file.path(sb, "r-installation", "library"),
      file.path(dirname(sb), "r-packages", "library"),
      file.path(dirname(dirname(sb)), "r-runtime", "library")
    )
    for (bl in bundled_libs) {
      if (dir.exists(bl)) {
        active_libs <- c(active_libs, bl)
      }
    }
  }
  if (dir.exists("r-runtime/library")) {
    active_libs <- c(active_libs, file.path(getwd(), "r-runtime", "library"))
  }

  if (length(active_libs) > 0) {
    .libPaths(unique(c(active_libs, .libPaths())))
  }

  # Fast check: are all required packages already available?
  missing_packages <- c()
  for (pkg in packages_list) {
    if (!requireNamespace(pkg, quietly = TRUE)) {
      missing_packages <- c(missing_packages, pkg)
    }
  }

  # Fast path: all packages are available, load immediately without network
  if (length(missing_packages) == 0) {
    packages_to_load <- unique(c(packages_list, "grid"))
    for (i in seq_along(packages_to_load)) {
      if (exists("canreg_progress", mode = "function")) {
        canreg_progress(pb, i / (length(packages_to_load) + 1), "Loading R packages", sprintf("Loading package - %s", packages_to_load[i]))
      } else if (sysName == "Windows" && exists("setWinProgressBar", mode = "function")) {
        setWinProgressBar(pb, value = i / (length(packages_to_load) + 1), label = sprintf("Loading package - %s", packages_to_load[i]))
      }
      suppressPackageStartupMessages(require(packages_to_load[i], character.only = TRUE, quietly = TRUE))
    }
    if (exists("canreg_progress", mode = "function")) {
      canreg_progress(pb, 1, "Loading R packages", "Done")
    } else if (sysName == "Windows" && exists("setWinProgressBar", mode = "function")) {
      setWinProgressBar(pb, 1, title = "Loading R packages", label = "Done")
    }
    if (!is.null(pb)) {
      try(close(pb), silent = TRUE)
    }
    return(invisible(TRUE))
  }

  # Slow path: missing packages need to be installed from CRAN
  # First verify internet connectivity
  has_internet <- FALSE
  try({
    con <- url("https://cran.r-project.org", open = "rb", timeout = 5)
    close(con)
    has_internet <- TRUE
  }, silent = TRUE)

  if (!has_internet) {
    if (!is.null(pb)) {
      try(close(pb), silent = TRUE)
    }
    stop(paste0(
      "Missing required R package(s): ", paste(missing_packages, collapse = ", "), ".\n",
      "No internet connection to CRAN is available to download them automatically.\n",
      "Please connect to the internet, install the packages, or use the CanReg5 bundle with R included."
    ))
  }

  # Online installation for missing packages
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))
  new.repos <- old.repos
  new.repos["CRAN"] <- "https://cran.r-project.org"
  options(repos = new.repos)

  if (sysName == "Windows") {
    options(pkgType = "win.binary") 
  }

  for (i in seq_along(missing_packages)) {
    if (exists("canreg_progress", mode = "function")) {
      canreg_progress(pb, i / (length(missing_packages) + 1), "Downloading R packages", sprintf("Installing %s...", missing_packages[i]))
    } else if (sysName == "Windows" && exists("setWinProgressBar", mode = "function")) {
      setWinProgressBar(pb, value = i / (length(missing_packages) + 1), label = sprintf("%s (Installing...)", missing_packages[i]))
    }
    try(install.packages(missing_packages[i], lib = user_lib, dependencies = TRUE, quiet = TRUE), silent = TRUE)
  }

  # Load all packages
  packages_to_load <- unique(c(packages_list, "grid"))
  for (i in seq_along(packages_to_load)) {
    if (exists("canreg_progress", mode = "function")) {
      canreg_progress(pb, i / (length(packages_to_load) + 1), "Loading R packages", sprintf("Loading package - %s", packages_to_load[i]))
    } else if (sysName == "Windows" && exists("setWinProgressBar", mode = "function")) {
      setWinProgressBar(pb, value = i / (length(packages_to_load) + 1), label = sprintf("Loading package - %s", packages_to_load[i]))
    }
    suppressPackageStartupMessages(require(packages_to_load[i], character.only = TRUE, quietly = TRUE))
  }

  if (exists("canreg_progress", mode = "function")) {
    canreg_progress(pb, 1, "Loading R packages", "Done")
  } else if (sysName == "Windows" && exists("setWinProgressBar", mode = "function")) {
    setWinProgressBar(pb, 1, title = "Loading R packages", label = "Done")
  }
  if (!is.null(pb)) {
    try(close(pb), silent = TRUE)
  }
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
  remote_translation_folder <- "https://raw.githubusercontent.com/IARC-CSU/CanReg5/master/conf/tables/r/r-translations/"

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

    canreg_update_source(paste0(remote_translation_folder,"translation_es.csv"))
    canreg_update_source(paste0(remote_translation_folder,"translation_pt.csv"))
    canreg_update_source(paste0(remote_translation_folder,"translation_ru.csv"))

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
  else if (grepl("r-translations", folder_url))
  {
    dt_local <- fread(local_file)
    dt_remote <-fread(url)
    if (!isTRUE(all.equal(dt_local, dt_remote)))
    {
      remote_version <- 2
      local_version <- 1
    }

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




