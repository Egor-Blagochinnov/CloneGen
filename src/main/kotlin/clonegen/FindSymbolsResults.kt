package org.blagochinnoved.clonegen

import com.google.devtools.ksp.symbol.KSAnnotated

data class FindSymbolsResults(
    val valid: Sequence<KSAnnotated>,
    val invalid: Sequence<KSAnnotated>,
)