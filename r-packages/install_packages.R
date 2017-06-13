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
    "assertthat_0.2.0.zip",
    "base64enc_0.1-3.zip",
    "bindr_0.1.zip",
    "bindrcpp_0.1.zip",
    "BH_1.62.0-1.zip",
    "Cairo_1.5-9.zip",
    "colorspace_1.3-2.zip",
    "data.table_1.10.4.zip",
    "DBI_0.6-1.zip",
    "dichromat_2.0-0.zip",
    "digest_0.6.12.zip",
    "dplyr_0.7.0.zip",
    "evaluate_0.10.zip",
    "gdtools_0.1.4.zip",
    "ggplot2_2.2.1.zip",
    "glue_1.0.0.zip",
    "gridExtra_2.2.1.zip",
    "gtable_0.2.0.zip",
    "highr_0.6.zip",
    "htmltools_0.3.6.zip",
    "httpuv_1.3.3.zip",
    "jsonlite_1.5.zip",
    "knitr_1.16.zip",
    "labeling_0.3.zip",
    "lazyeval_0.2.0.zip",
    "magrittr_1.5.zip",
    "markdown_0.8.zip",
    "mime_0.5.zip",
    "munsell_0.4.3.zip",
    "officer_0.1.4.zip",
    "pkgconfig_2.0.1.zip",
    "plogr_0.1-1.zip",
    "plyr_1.8.4.zip",
    "png_0.1-7.zip",
    "purrr_0.2.2.2.zip",
    "R.methodsS3_1.7.1.zip",
    "R.oo_1.21.0.zip",
    "R.utils_2.5.0.zip",
    "R6_2.2.1.zip",
    "RColorBrewer_1.1-2.zip",
    "Rcpp_0.12.10.zip",
    "ReporteRsjars_0.0.2.zip",
    "ReporteRs_0.8.8.zip",
    "reshape2_1.4.2.zip",
    "rJava_0.9-8.zip",
    "rlang_0.1.1.zip",
    "rvg_0.1.3.zip",
    "scales_0.4.1.zip",
    "shiny_1.0.3.zip",
    "sourcetools_0.1.6.zip",
    "stringi_1.1.5.zip",
    "stringr_1.2.0.zip",
    "tibble_1.3.3.zip",
    "uuid_0.1-2.zip",
    "withr_1.0.2.zip",
    "xml2_1.1.1.zip",
    "xtable_1.8-2.zip",
    "yaml_2.1.14.zip",
    "zip_1.0.0.zip"
)

for(x in packages) {
	eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"', repos = NULL  )", sep=""))) 
}