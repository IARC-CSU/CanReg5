load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 
  new.repos["CRAN"] <- "http://cran.stat.ucla.edu" #set your favorite  CRAN Mirror here 
  options(repos = new.repos) 
  x <- as.character(substitute(x)) 
  eval(parse(text=paste("install.packages('", x, "')", sep=""))) 
  eval(parse(text=paste("require(", x, ")", sep=""))) 
}