package trans;

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

@SuppressWarnings("all") final class lifted29 extends Strategy 
{ 
  public static final lifted29 instance = new lifted29();

  @Override public IStrategoTerm invoke(Context context, IStrategoTerm term)
  { 
    Fail235:
    { 
      IStrategoTerm w_10718 = null;
      if(term.getTermType() != IStrategoTerm.APPL || Main._consDesugared_1 != ((IStrategoAppl)term).getConstructor())
        break Fail235;
      w_10718 = term.getSubterm(0);
      term = w_10718;
      if(true)
        return term;
    }
    return null;
  }
}