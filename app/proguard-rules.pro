# === Smart Nav Pill — regras R8/ProGuard ===

# O AccessibilityService e instanciado pelo sistema a partir do Manifest; o
# nome da classe NAO pode ser alterado nem removido. Mantemos o pacote inteiro
# do servico por seguranca.
-keep class com.smartnavpill.service.NavPillService { *; }
-keep class com.smartnavpill.service.** { *; }

# A Application tambem e referenciada pelo nome no Manifest.
-keep class com.smartnavpill.SmartNavPillApp { *; }

# Mantem os metadados do Kotlin (necessario para reflexao de coroutines/flows).
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Jetpack Compose e DataStore ja trazem regras consumidas automaticamente
# (consumer rules), entao nao e necessario adicionar mais nada para eles.
