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

@SuppressWarnings("all") final class lifted353 extends Strategy 
{ 
  public static final lifted353 instance = new lifted353();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail1665:
    { 
      if(term.getTermType() != IStrategoTerm.APPL || Main._consHide_0 != ((IStrategoAppl)term).getConstructor())
        break Fail1665;
      if(true)
        return term;
    }
    return null;
  }
}