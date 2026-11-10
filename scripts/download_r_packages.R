# scripts/download_r_packages.R
# Automates downloading Windows binary CRAN packages for the CanReg5 offline R-packages bundle.

args <- commandArgs(trailingOnly = TRUE)
destdir <- if (length(args) > 0) args[1] else "conf/tables/r/r-packages"
r_version <- if (length(args) > 1) args[2] else NULL

dir.create(destdir, recursive = TRUE, showWarnings = FALSE)

cran_mirror <- "https://cloud.r-project.org"
options(repos = c(CRAN = cran_mirror))

contrib_url <- if (!is.null(r_version)) {
  paste0(cran_mirror, "/bin/windows/contrib/", r_version)
} else {
  contrib.url(cran_mirror, type = "win.binary")
}

cat(sprintf("=== CanReg5 Offline R Package Downloader ===\n"))
cat(sprintf("Destination directory: %s\n", normalizePath(destdir, mustWork = FALSE)))
cat(sprintf("CRAN Windows binary URL: %s\n", contrib_url))

cat("Fetching available Windows binary packages from CRAN...\n")
av <- tryCatch({
  available.packages(contriburl = contrib_url, type = "win.binary")
}, error = function(e) {
  # Fallback to current release contrib URL if specific version not found
  fallback_url <- contrib.url(cran_mirror, type = "win.binary")
  cat(sprintf("Specified contrib URL failed (%s). Falling back to: %s\n", e$message, fallback_url))
  available.packages(contriburl = fallback_url, type = "win.binary")
})

core_packages <- c(
  "Rcpp", "data.table", "ggplot2", "shiny", "shinydashboard", "shinyjs",
  "gridExtra", "scales", "Cairo", "officer", "flextable", "zip",
  "bmp", "jpeg", "png", "shiny.i18n", "XML", "plyr", "stringr",
  "dplyr", "RJSONIO", "jsonlite", "lubridate", "reshape2", "tidyr",
  "gtools", "Rcan"
)

valid_packages <- intersect(core_packages, rownames(av))
missing_core <- setdiff(core_packages, rownames(av))
if (length(missing_core) > 0) {
  cat(sprintf("Notice: Core packages not present in repository (might be base R or source-only): %s\n",
              paste(missing_core, collapse = ", ")))
}

cat(sprintf("Found %d core packages in Windows binary repository.\n", length(valid_packages)))
cat("Resolving recursive dependencies (Depends, Imports, LinkingTo)...\n")

deps <- tools::package_dependencies(
  valid_packages,
  db = av,
  recursive = TRUE,
  which = c("Depends", "Imports", "LinkingTo")
)

all_packages <- unique(c(valid_packages, unlist(deps)))
all_packages <- intersect(all_packages, rownames(av))

cat(sprintf("Total packages to download: %d\n", length(all_packages)))

downloaded <- download.packages(
  pkgs = all_packages,
  destdir = destdir,
  available = av,
  contriburl = contrib_url,
  type = "win.binary",
  quiet = FALSE
)

cat(sprintf("=== Successfully downloaded %d Windows binary packages to %s ===\n",
            nrow(downloaded), destdir))

