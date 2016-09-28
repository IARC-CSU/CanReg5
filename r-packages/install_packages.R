## Find directory of script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

setwd(script.basename);

dir.create(Sys.getenv("R_LIBS_USER"), recursive = TRUE)
rlibs = gsub("\\", "/", Sys.getenv("R_LIBS_USER"), fixed = T)

## TODO: Make this dynamic...
packages = c(
    "bitops_1.0-6.zip",
    "caTools_1.17.1.zip",
    "colorspace_1.2-4.zip",
    "dichromat_2.0-0.zip",
    "digest_0.6.4.zip",
    "gdata_2.13.3.zip",
    "ggplot2_0.9.3.1.zip",
    "gplots_2.14.2.zip",
    "gtable_0.1.2.zip",
    "gtools_3.4.1.zip",
    "labeling_0.2.zip",
    "munsell_0.4.2.zip",
    "plyr_1.8.zip",
    "proto_0.3-10.zip",
    "RColorBrewer_1.0-5.zip",
    "reshape2_1.2.2.zip",
    "scales_0.4.0.zip",
    "stringr_0.6.2.zip",
    "Rcpp_0.12.7.zip"
)

for(x in packages) {
	eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep=""))) 
}