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

@SuppressWarnings("all") public class $Single$Line$Template_1_0 extends Strategy 
{ 
  public static $Single$Line$Template_1_0 instance = new $Single$Line$Template_1_0();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term, Strategy e_9756)
  { 
    ITermFactory termFactory = context.getFactory();
    context.push("SingleLineTemplate_1_0");
    Fail519:
    { 
      IStrategoTerm c_9938 = null;
      IStrategoTerm b_9938 = null;
      if(term.getTermType() != IStrategoTerm.APPL || Main._consSingleLineTemplate_1 != ((IStrategoAppl)term).getConstructor())
        break Fail519;
      b_9938 = term.getSubterm(0);
      IStrategoList annos92 = term.getAnnotations();
      c_9938 = annos92;
      term = e_9756.invoke(context, b_9938);
      if(term == null)
        break Fail519;
      term = termFactory.annotateTerm(termFactory.makeAppl(Main._consSingleLineTemplate_1, new IStrategoTerm[]{term}), checkListAnnos(termFactory, c_9938));
      context.popOnSuccess();
      if(true)
        return term;
    }
    context.popOnFailure();
    return null;
  }
}