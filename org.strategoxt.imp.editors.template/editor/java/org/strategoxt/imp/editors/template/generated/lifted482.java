package org.strategoxt.imp.editors.template.generated;

import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_lib.*;
import org.strategoxt.stratego_sglr.*;
import org.strategoxt.stratego_gpp.*;
import org.strategoxt.stratego_xtc.*;
import org.strategoxt.stratego_aterm.*;
import org.strategoxt.stratego_sdf.*;
import org.strategoxt.strc.*;
import org.strategoxt.imp.editors.template.generated.*;
import org.strategoxt.java_front.*;
import org.strategoxt.lang.*;
import org.spoofax.interpreter.terms.*;
import static org.strategoxt.lang.Term.*;
import org.spoofax.interpreter.library.AbstractPrimitive;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

@SuppressWarnings("all") final class lifted482 extends Strategy 
{ 
  public static final lifted482 instance = new lifted482();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1506:
    { 
      if(term.getTermType() != IStrategoTerm.STRING || !"completion".equals(((IStrategoString)term).stringValue()))
        break Fail1506;
      if(true)
        return term;
    }
    return null;
  }
}