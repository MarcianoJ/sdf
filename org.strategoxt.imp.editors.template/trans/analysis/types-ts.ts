module analysis/types-ts

imports 
  include/TemplateLang
  libstrc
  analysis/types

type rules
	
	
	//When a sort has multiple definitions something like
    //Iter(Sort(s)) : ListType(s) doesn't work properly
    //In this case, I just want the type for Iter(Sort("A")) to be ListType("A")
    //but A carry out its annotations which include the multiple definitions
	
	//Iter(Sort(s)) : ListType(s)
	
    Lex(sort@Sort(s)) : t
    	where sort: t
    	
    Cf(sort@Sort(s)) : t
    	where sort: t
    
    Placeholder(_, s, None(), _) : t
    	where s : t
    	
    
    	
    	
    