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

@SuppressWarnings("all") public class sdf_completion_1_0 extends Strategy 
{ 
  public static sdf_completion_1_0 instance = new sdf_completion_1_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy y_32795)
  { 
    context.push("sdf_completion_1_0");
    Fail1377:
    { 
      lifted497 lifted4970 = new lifted497();
      lifted4970.y_32795 = y_32795;
      term = term_1_0.instance.invoke(context, term, lifted4970);
      if(term == null)
        break Fail1377;
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}