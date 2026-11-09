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

load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))
  new.repos <- old.repos 
  new.repos["CRAN"] <- "https://cran.r-project.org"
  options(repos = new.repos)
  x <- as.character(substitute(x))
  rlibs <- gsub("\\", "/", canreg_get_user_lib(), fixed = TRUE)
  dir.create(rlibs, recursive = TRUE, showWarnings = FALSE)
  eval(parse(text = paste("install.packages('", x, "', lib='", rlibs, "', quiet = TRUE)", sep = ""))) 
  eval(parse(text = paste("require(", x, ", lib.loc = '", rlibs, "')", sep = "")))
}

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[, 1]) 

canreg_load_packages <- function(packages_list) { 

  sysName <- Sys.info()[['sysname']]

  user_lib <- canreg_get_user_lib()
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
