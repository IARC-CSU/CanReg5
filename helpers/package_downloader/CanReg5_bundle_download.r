
## Download Windows R packages bundle for CanReg5

# Determine target directory (defaults to conf/tables/r/r-packages)
script_dir <- dirname(normalizePath(sys.frame(1)$ofile, mustWork = FALSE))
repo_root <- normalizePath(file.path(script_dir, "..", ".."), mustWork = FALSE)
destdir <- file.path(repo_root, "conf", "tables", "r", "r-packages")

downloader_script <- file.path(repo_root, "scripts", "download_r_packages.R")
if (file.exists(downloader_script)) {
  source(downloader_script)
} else {
  stop("Downloader script not found at: ", downloader_script)
}



