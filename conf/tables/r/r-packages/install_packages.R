## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

setwd(script.basename)

# Clean install in r\3.4\canreg5.
unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
.libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))
rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)

## Detect all package in bundle folder
packages  <- list.files(pattern=".*\\.zip$")

for(x in packages) {
eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep=""))) 
}

## install Rcan from source

Rcan_file <- list.files(path=script.basename, pattern= "Rcan_\\d\\.\\d\\.\\d+\\.tar\\.gz")
Rcan_version <- regmatches(Rcan_file,regexpr(pattern= "\\d\\.\\d\\.\\d+", Rcan_file))
install.packages(paste0(script.basename, "/",Rcan_file), repos=NULL, type="source")