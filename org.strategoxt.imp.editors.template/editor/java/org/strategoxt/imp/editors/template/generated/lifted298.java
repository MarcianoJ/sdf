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

@SuppressWarnings("all") final class lifted298 extends Strategy 
{ 
  Strategy p_9757;

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail666:
    { 
      lifted299 lifted2990 = new lifted299();
      lifted2990.p_9757 = p_9757;
      term = default_1_0.instance.invoke(context, term, lifted2990);
      if(term == null)
        break Fail666;
      if(true)
        return term;
    }
    return null;
  }
}