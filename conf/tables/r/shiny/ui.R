
library(shinydashboard)
library(shinyjs)

ui <- dashboardPage(

  title="Canreg5 interactive graph",
  dashboardHeader(	title = HTML(
      "<div style = 'vertical-align:middle'>
       <img src = 'LogoBetaNew64x64.ico' class='canreg_logo' align = 'center'>
       </div>"),
       titleWidth = 350),
  
	

	
  dashboardSidebar(
    
    tags$link(rel = "stylesheet", type = "text/css", href = "registry_graph.css"),
    width = 350,
    
    tags$div(class="subHeader", checked=NA,
             tags$p("Graphics")
    ),
    		
		uiOutput("UI_select_table"),
		
		tags$div(class="subHeader", checked=NA,
		         tags$p("Export Graph")
		),
		
		selectInput("select_format", "Format", as.list(c( "pdf","tiff","png", "svg", "ps","csv"))),
		
		textInput("text_filename", "Filename", "CanReg5_graph"),
		
		downloadButton('downloadFile', 'Export graph', class="mat_btn"),
		
		tags$div(class="subHeader", checked=NA,
		         tags$p("Powerpoint presentation")
		),
		
		actionButton('actionSlide', "Add to presentation",  class="mat_btn"),
		
		textInput("pptx_filename", "Powerpoint filename", "CanReg5_slide"),
		
		downloadButton('downloadPres', 'Create presentation',  class="mat_btn"),
		
		uiOutput("UI_nbSlide")
  	
	   
	 ),
	  
	  
  
												
	dashboardBody(
	
		tags$script(HTML(paste0('$(document).ready(function(){
											$("header").find("nav").append(\'<div class="regtitle">',ls_args$header,'</div>\');
                    })
   '))),
	  useShinyjs(),
		downloadButton('downloadLog', '', class="log"),
		downloadButton('downloadData', '', class="log"),
	  fluidRow(

	      plotOutput("plot", height ="600px")

	   ),
	  fluidRow(
	    box(id="controls_COL1",
	      uiOutput("UI_control1"),
	      uiOutput("UI_control2")
	    ),
	    box(id="controls_COL2",
  	    uiOutput("UI_control3"),
  	    uiOutput("UI_control4")
	    )
	  )
	)
)