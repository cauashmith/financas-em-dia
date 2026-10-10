package br.com.financasemdia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.financasemdia.domain.model.Category

val Category.icon: ImageVector
    get() = when (this) {
        Category.SALARY -> Icons.Outlined.Payments
        Category.EXTRA_INCOME -> Icons.Outlined.Work
        Category.INVESTMENTS -> Icons.AutoMirrored.Outlined.TrendingUp
        Category.FOOD -> Icons.Outlined.Restaurant
        Category.HOUSING -> Icons.Outlined.Home
        Category.TRANSPORT -> Icons.Outlined.DirectionsCar
        Category.HEALTH -> Icons.Outlined.LocalHospital
        Category.EDUCATION -> Icons.Outlined.School
        Category.LEISURE -> Icons.Outlined.SportsEsports
        Category.BILLS -> Icons.Outlined.Receipt
        Category.OTHER -> Icons.Outlined.Category
    }

@Composable
fun CategoryIcon(category: Category, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
