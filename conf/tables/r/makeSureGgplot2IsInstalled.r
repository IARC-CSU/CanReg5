load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))
  new.repos <- old.repos 
  new.repos["CRAN"] <- "https://cran.r-project.org"
  options(repos = new.repos)
  x <- as.character(substitute(x))
  rlibs <- gsub("\\", "/", file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = TRUE)
  dir.create(rlibs, recursive = TRUE, showWarnings = FALSE)
  eval(parse(text = paste("install.packages('", x, "', lib='", rlibs, "', quiet = TRUE)", sep = ""))) 
  eval(parse(text = paste("require(", x, ", lib.loc = '", rlibs, "')", sep = "")))
}

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[, 1]) 

canreg_load_packages <- function(packages_list) { 

  sysName <- Sys.info()[['sysname']]

  r_user_env <- Sys.getenv("R_LIBS_USER")
  if (nzchar(r_user_env)) {
    user_lib <- paste0(r_user_env, "-CanReg5")
  } else {
    user_lib <- file.path(path.expand("~"), ".R-CanReg5-library")
  }
  if (!dir.exists(user_lib)) {
    try(dir.create(user_lib, recursive = TRUE, showWarnings = FALSE), silent = TRUE)
  }

  if (dir.exists(user_lib)) {
    .libPaths(unique(c(user_lib, .libPaths())))
  }

  missing_packages <- c()
  for (pkg in packages_list) {
    if (!requireNamespace(pkg, quietly = TRUE)) {
      missing_packages <- c(missing_packages, pkg)
    }
  }

  if (length(missing_packages) == 0) {
    for (pkg in packages_list) {
      suppressPackageStartupMessages(require(pkg, character.only = TRUE, quietly = TRUE))
    }
    return(invisible(TRUE))
  }

  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))
  new.repos <- old.repos
  new.repos["CRAN"] <- "https://cran.r-project.org"
  options(repos = new.repos)

  if (sysName == "Windows") {
    options(pkgType = "win.binary") 
  }

  for (pkg in missing_packages) {
    try(install.packages(pkg, lib = user_lib, dependencies = TRUE, quiet = TRUE), silent = TRUE)
  }

  for (pkg in packages_list) {
    suppressPackageStartupMessages(require(pkg, character.only = TRUE, quietly = TRUE))
  }
}

canreg_load_packages(c("ggplot2", "Cairo", "RColorBrewer"))
