## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

setwd(script.basename);

# Clean install in r\3.3\canreg5.
unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)


## TODO: Make this dynamic... DONE?
packages  <- list.files(pattern=".*\\.zip$")

for(x in packages) {
  eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep=""))) 
}