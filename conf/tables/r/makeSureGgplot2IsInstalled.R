load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos))               #this resets the repos option when the function exits 
  new.repos <- old.repos 
  new.repos["CRAN"] <- "http://cran.rstudio.com/"   #Rstudio, automatic redirection to servers worldwide , or set your favorite  CRAN Mirror here 
  options(repos = new.repos)
  x <- as.character(substitute(x))
  rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)
  dir.create(rlibs, recursive = TRUE)
  eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"' )", sep=""))) 
  eval(parse(text=paste("require(", x, ", lib.loc = '", rlibs ,"')", sep="")))
}

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

if(!is.installed("ggplot2")){
	load.fun("ggplot2")
}

require(ggplot2)
